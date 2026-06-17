# 安全策略

## 敏感信息

不要提交以下信息：

- GitHub、Google、云服务账号密码
- API Key、Token、证书、私钥
- 真实摄像头 RTSP 地址、GB28181 SIP 密码、厂商 SDK 密钥
- 客户现场拓扑、坐标、视频截图、告警证据

项目使用 `.env` 存放本地配置，`.env` 已加入 `.gitignore`。

## 私有仓库阶段

正式公开前，GitHub 仓库应保持 Private。公开前需要完成脱敏检查、许可证确认、演示数据替换和安全说明补齐。

## 漏洞反馈

当前阶段先通过私有 Issue 或仓库维护者私下沟通处理安全问题。后续公开后再启用 GitHub Security Advisories。
