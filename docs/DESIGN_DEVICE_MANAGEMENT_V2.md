<!--
  DESIGN_DEVICE_MANAGEMENT_V2.md
  设备管理 v2.0 设计文档
  目标：层级设备管理 + GB/T 28181 国标协议接入
-->

# 设备管理 v2.0 设计文档

## 1. 问题分析

### 当前问题
- 设备模型是扁平的 `Device` record，只靠 `sourceType` 字段区分类型
- 没有父子层级关系（NVR 无法挂载摄像头通道）
- 设备适配器只是空壳，Mock 实现无实际协议对接
- `GET /api/devices/{id}` 返回 405（缺少路由）
- 前端列表视图，无法展示设备层级

### 目标架构
```
NVR 设备（GB/T 28181 接入）
├── 通道 1：厂区大门枪机
├── 通道 2：危化仓库球机
├── 通道 3：围界红外枪机
└── ...

无人机机场（DJI Dock）
├── 无人机 1
│   └── 云台相机

车辆终端（JT/T 1078）
├── 通道 1：车头摄像头
├── 通道 2：车厢摄像头
└── ...
```

## 2. GB/T 28181 协议入门

### 是什么
GB/T 28181 是《公共安全视频监控联网系统信息传输、交换、控制技术要求》，中国国家标准。
它定义了视频监控平台之间、平台与设备之间的通信协议。

### 协议栈
```
┌─────────────────────┐
│   SIP (信令控制)      │  ← 设备注册、目录查询、视频请求
├─────────────────────┤
│   SDP (媒体协商)      │  ← 编解码、传输方式协商
├─────────────────────┤
│   RTP/RTCP (媒体传输) │  ← 音视频流传输
├─────────────────────┤
│   MANSCDP / MANSRTSP │  ← 国标扩展的会话描述和媒体控制
└─────────────────────┘
```

### 设备接入流程（核心三步）

```
第一步：REGISTER（注册）
NVR ──── SIP REGISTER ────▶ 平台（我们）
平台 ──── 200 OK ────────▶ NVR
说明：NVR 定期发送注册消息，携带设备 ID 和密码

第二步：Catalog（获取目录）
平台 ──── SIP MESSAGE ────▶ NVR  （请求设备列表）
NVR  ──── SIP MESSAGE ────▶ 平台  （返回 XML 设备列表）
说明：平台通过 MESSAGE 消息发送 Catalog 查询，NVR 返回 XML 格式的设备信息

第三步：INVITE（播放视频）
平台 ──── SIP INVITE ─────▶ NVR  （请求播放某通道视频）
NVR  ──── 200 OK + SDP ───▶ 平台  （返回媒体流地址）
说明：SDP 中携带 RTP/RTSP 流地址，后续通过 SRS/ZLMediaKit 转码转流
```

### 设备 ID 编码（20 位）
```
┌────┬────┬──┬──────────┬──┐
│中心│行业│类型│   序号    │扩展│
│编码│编码│编码│          │   │
│ 8  │ 2  │ 3 │    5     │ 2 │
└────┴────┴──┴──────────┴──┘
```

### Java SIP 库选择
| 方案 | 说明 | 推荐度 |
|------|------|--------|
| JAIN SIP (jain-sip-ri) | 标准 SIP 协议栈，完整但较重 | ⭐⭐⭐⭐ |
| MJSIP | 轻量级 Java SIP 库 | ⭐⭐⭐ |
| 自研 Netty SIP | 基于 Netty 手写 SIP 解析 | ⭐⭐（工作量大） |

**推荐：JAIN SIP**，Spring 生态有现成集成方案。

## 3. 新领域模型

```java
// 设备节点（抽象父类）
DeviceNode {
    id, name, deviceType, status, parentId, children, createdAt, updatedAt
}

// NVR 设备
NvrDevice extends DeviceNode {
    gbDeviceId,    // 国标 20 位设备 ID
    gbPassword,     // 国标注册密码
    sipHost, sipPort,      // SIP 服务器地址（平台侧）
    channelCount, channels // 通道列表
}

// 摄像头通道（挂载在 NVR 或车辆下）
ChannelNode extends DeviceNode {
    channelNo,      // 通道编号（从 0 开始）
    ptzType,        // 云台类型：fixed / ptz
    streamUrl,      // 播放地址
    resolution, aiEnabled
}

// 无人机机场
DroneDockNode extends DeviceNode {
    dockId, drones, status, batteryPercent
}

// 车载终端
VehicleNode extends DeviceNode {
    plateNo, vehicleType, speedKph, gps, channels
}
```

## 4. 新 API 设计

```
GET    /api/devices/tree           → 返回设备树（层级结构）
GET    /api/devices/nvrs           → NVR 设备列表
POST   /api/devices/nvrs           → 添加 NVR
GET    /api/devices/nvrs/{id}      → NVR 详情 + 通道列表
PUT    /api/devices/nvrs/{id}      → 更新 NVR
DELETE /api/devices/nvrs/{id}      → 删除 NVR
POST   /api/devices/nvrs/{id}/sync → 同步通道（从 NVR 获取最新通道列表）

GET    /api/devices/drones         → 无人机列表
POST   /api/devices/drones         → 添加无人机
GET    /api/devices/drones/{id}    → 无人机详情

GET    /api/devices/vehicles       → 车辆列表
POST   /api/devices/vehicles       → 添加车辆
GET    /api/devices/vehicles/{id}  → 车辆详情

POST   /api/devices/nvrs/{id}/channels/{ch}/play   → 播放通道视频
POST   /api/devices/nvrs/{id}/channels/{ch}/ptz    → 云台控制
```

## 5. 实施路线图

### Phase 1: 领域模型重设计（本会话）
- [ ] 创建 `DeviceNode` 抽象基类
- [ ] 创建 `NvrDevice`, `ChannelNode`, `DroneDockNode`, `VehicleNode`
- [ ] 重构 Repository 接口和 InMemory 实现
- [ ] 重构 Service 层
- [ ] 重构 PlatformController（新 API）
- [ ] 保留旧 `/api/devices` 兼容端点
- [ ] 更新文档

### Phase 2: GB/T 28181 SIP 服务器（下个会话）
- [ ] 引入 JAIN SIP 依赖
- [ ] 实现 SIP Server 监听
- [ ] 实现 REGISTER 处理
- [ ] 实现 Catalog 查询
- [ ] 实现 INVITE 播放
- [ ] NVR 通道自动同步

### Phase 3: 前端设备管理重构
- [ ] 树形设备管理视图
- [ ] NVR/无人机/车辆 分类 Tab
- [ ] 添加 NVR 对话框（含国标参数）
- [ ] 通道列表 + 实时预览

### Phase 4: 视频转码与播放
- [ ] ZLMediaKit 集成
- [ ] RTP → RTMP/FLV/HLS 转码
- [ ] 前端播放器集成

## 6. GB/T 28181 接入第一步（最小闭环）

当你有一个真实的 NVR 设备时，只需要：
1. 在 NVR 上配置 SIP 服务器地址 → 我们的后端 IP:5060
2. 在平台上添加 NVR（填入国标设备 ID 和注册密码）
3. 平台启动 SIP Server 监听 5060 端口
4. NVR 自动注册上来
5. 平台发起 Catalog 查询获取通道列表
6. 点击播放即可拉流

如果不确定 NVR 参数，登录 NVR Web 管理页 → 网络设置 → GB/T 28181 配置页，
那里有：SIP 服务器 IP、端口、设备 ID（20位）、注册密码、通道配置。
