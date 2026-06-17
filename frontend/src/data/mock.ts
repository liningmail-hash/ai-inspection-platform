export type Kpi = {
  label: string
  value: string
  trend: string
  tone: 'success' | 'warning' | 'primary'
}

export type Alarm = {
  id: string
  level: string
  type: string
  device: string
  status: string
  time: string
  evidenceUrl?: string
}

export type Device = {
  id?: string
  name: string
  sourceType?: 'camera' | 'drone' | 'vehicle'
  vendor: string
  protocol: string
  endpoint?: string
  credentialRef?: string
  location?: string
  edgeNodeId?: string
  createdAt?: string
  updatedAt?: string
  status: string
  bitrate: string
  latency: string
}

export type InspectionPlan = {
  id?: string
  name: string
  points: number
  algorithm: string
  schedule: string
  status: string
}

export type Model = {
  id?: string
  algorithm: string
  version: string
  precision: string
  recall: string
  status: string
}

export type DroneSummary = {
  dock: string
  status: string
  battery: number
  weather: string
  route: string
  telemetry: string[]
}

export type InspectionTask = {
  id: string
  name: string
  type: string
  priority: string
  status: string
  route: string
  assignee: string
  plannedAt: string
}

export type MediaAsset = {
  id: string
  name: string
  assetType: string
  source: string
  relatedTask: string
  status: string
  url: string
  capturedAt: string
}

export type InspectionReport = {
  id: string
  title: string
  period: string
  status: string
  format: string
  generatedAt: string
  downloadUrl: string
}

export type AlgorithmParameter = {
  id: string
  algorithmCode: string
  algorithmName: string
  threshold: number
  sensitivity: number
  enabled: boolean
  updatedAt: string
}

export type MapEvent = {
  id: string
  title: string
  type: string
  severity: string
  status: string
  latitude: number
  longitude: number
  source: string
  detectedAt: string
}

export type SystemUser = {
  id: string
  username: string
  displayName: string
  organization: string
  site: string
  status: string
  roles: string[]
  permissions: string[]
}

export type SystemRole = {
  id: string
  code: string
  name: string
  description: string
  permissions: string[]
  userCount: number
}

export type AuditLog = {
  id: string
  actor: string
  action: string
  targetType: string
  targetId: string
  result: string
  createdAt: string
}

export type IntegrationConfig = {
  id: string
  name: string
  sourceType: 'camera' | 'drone' | 'vehicle'
  vendor: string
  sdkType: string
  status: string
  endpoint: string
  credentialRef: string
  lastSyncAt: string
  channelCount: number
}

export type VideoChannel = {
  id: string
  sourceId: string
  sourceType: 'camera' | 'drone' | 'vehicle'
  sourceName: string
  name: string
  protocol: string
  streamUrl: string
  playUrl: string
  aiEnabled: boolean
  status: string
  edgeNode: string
  latitude: number
  longitude: number
}

export type VideoSession = {
  id: string
  channelId: string
  playUrl: string
  protocol: string
  status: string
  startedAt: string
  expiresAt: string
}

export type DroneAsset = {
  id: string
  dockId: string
  name: string
  vendor: string
  status: string
  batteryPercent: number
  latitude: number
  longitude: number
  telemetry: string[]
  activeTask: string
}

export type FlightRoute = {
  id: string
  dockId: string
  name: string
  waypointCount: number
  altitudeMeter: number
  status: string
}

export type FlightTask = {
  id: string
  routeName: string
  status: string
  plannedAt: string
}

export type VehicleAsset = {
  id: string
  plateNo: string
  name: string
  vendor: string
  status: string
  speedKph: number
  latitude: number
  longitude: number
  lastSeenAt: string
}

export type VehicleTrackPoint = {
  vehicleId: string
  latitude: number
  longitude: number
  speedKph: number
  heading: number
  sampledAt: string
}

export type AiTask = {
  id: string
  sourceType: 'camera' | 'drone' | 'vehicle'
  channelId: string
  algorithmCode: string
  modelVersion: string
  status: string
  confidence: number
  evidenceUrl: string
  createdAt: string
}

export const kpis: Kpi[] = [
  { label: '在线设备', value: '126/138', trend: '+4', tone: 'success' },
  { label: '今日告警', value: '37', trend: '-12%', tone: 'warning' },
  { label: 'AI任务运行', value: '16', trend: 'GPU 62%', tone: 'primary' },
  { label: '无人机任务', value: '3', trend: '1 执行中', tone: 'success' }
]

export const alarms: Alarm[] = [
  { id: 'ALM-20260615-001', level: '高', type: '烟火识别', device: 'A区危化仓-枪机01', status: '待确认', time: '21:03:18', evidenceUrl: '/evidence/alarm-001.jpg' },
  { id: 'ALM-20260615-002', level: '中', type: '人员闯入', device: 'B区围界-球机03', status: '处理中', time: '20:48:02', evidenceUrl: '/evidence/alarm-002.jpg' },
  { id: 'ALM-20260615-003', level: '低', type: '安全帽识别', device: 'C区装卸口-枪机02', status: '已关闭', time: '20:17:44', evidenceUrl: '/evidence/alarm-003.jpg' }
]

export const devices: Device[] = [
  { id: 'dev-001', name: 'A区危化仓-枪机01', sourceType: 'camera', vendor: 'HIKVISION', protocol: 'GB28181', endpoint: 'gb28181://camera-a1.local', credentialRef: 'secret://devices/dev-001', location: 'A区危化仓', edgeNodeId: 'EDGE-01', status: 'online', bitrate: '3.8Mbps', latency: '86ms' },
  { id: 'dev-002', name: 'B区围界-球机03', sourceType: 'camera', vendor: 'DAHUA', protocol: 'ONVIF', endpoint: 'onvif://camera-b3.local', credentialRef: 'secret://devices/dev-002', location: 'B区围界', edgeNodeId: 'EDGE-01', status: 'online', bitrate: '2.4Mbps', latency: '92ms' },
  { id: 'dev-003', name: 'C区装卸口-枪机02', sourceType: 'camera', vendor: 'MOCK_VENDOR', protocol: 'RTSP', endpoint: 'rtsp://example/live/c2', credentialRef: 'secret://devices/dev-003', location: 'C区装卸口', edgeNodeId: 'EDGE-01', status: 'online', bitrate: '2.1Mbps', latency: '77ms' },
  { id: 'dev-004', name: 'D区停车场-车载网关', sourceType: 'vehicle', vendor: 'JT1078', protocol: 'JT/T1078', endpoint: 'jt1078://gateway.local', credentialRef: 'secret://devices/dev-004', location: 'D区停车场', edgeNodeId: 'EDGE-02', status: 'offline', bitrate: '-', latency: '-' }
]

export const plans: InspectionPlan[] = [
  { id: 'plan-001', name: '危化仓烟火巡检', points: 12, algorithm: '烟火识别', schedule: '每 5 分钟', status: '运行中' },
  { id: 'plan-002', name: '围界人员闯入巡检', points: 18, algorithm: '人员闯入', schedule: '全天', status: '运行中' },
  { id: 'plan-003', name: '装卸区安全帽巡检', points: 9, algorithm: '安全帽识别', schedule: '工作日', status: '暂停' }
]

export const models: Model[] = [
  { id: 'model-001', algorithm: '烟火识别', version: 'v1.4.2', precision: '94.8%', recall: '91.6%', status: '生产' },
  { id: 'model-002', algorithm: '人员闯入', version: 'v1.2.0', precision: '96.1%', recall: '93.2%', status: '灰度' },
  { id: 'model-003', algorithm: '安全帽识别', version: 'v0.9.8', precision: '91.7%', recall: '88.5%', status: '候选' }
]

export const drone: DroneSummary = {
  dock: '一号机场',
  status: '就绪',
  battery: 87,
  weather: '风速 3.1m/s / 小雨',
  route: '东区围界固定航线',
  telemetry: ['ALT 86m', 'SPD 8.4m/s', 'HDG 126deg', 'LINK 98%', 'LAT 31.2304', 'LNG 121.4737']
}

export const inspectionTasks: InspectionTask[] = [
  { id: 'task-001', name: '危化仓烟火即时复核', type: 'immediate', priority: 'high', status: 'running', route: 'A区危化仓点位', assignee: '巡检值班员', plannedAt: '2026-06-16T09:20:00+08:00' },
  { id: 'task-002', name: '东区围界定时巡检', type: 'scheduled', priority: 'medium', status: 'scheduled', route: '东区围界固定航线', assignee: '无人机机场', plannedAt: '2026-06-16T10:00:00+08:00' },
  { id: 'task-003', name: '安全帽识别循环抽检', type: 'loop', priority: 'medium', status: 'queued', route: 'C区装卸口', assignee: '边缘节点 EDGE-01', plannedAt: '2026-06-16T10:30:00+08:00' }
]

export const mediaAssets: MediaAsset[] = [
  { id: 'media-001', name: 'A区危化仓烟火证据图', assetType: 'image', source: 'camera', relatedTask: '危化仓烟火巡检', status: 'stored', url: '/evidence/alarm-001.jpg', capturedAt: '2026-06-16T09:18:00+08:00' },
  { id: 'media-002', name: '东区围界无人机巡检视频', assetType: 'video', source: 'drone', relatedTask: '东区围界固定航线', status: 'stored', url: '/media/flight-001.mp4', capturedAt: '2026-06-16T09:28:00+08:00' },
  { id: 'media-003', name: '安全帽样本采集包', assetType: 'dataset', source: 'ai', relatedTask: '装卸区安全帽巡检', status: 'labeling', url: '/datasets/helmet-batch-001', capturedAt: '2026-06-16T09:35:00+08:00' }
]

export const inspectionReports: InspectionReport[] = [
  { id: 'report-001', title: '一号工业园日巡检报告', period: '2026-06-16', status: 'generated', format: 'PDF', generatedAt: '2026-06-16T09:40:00+08:00', downloadUrl: '/reports/daily-20260616.pdf' },
  { id: 'report-002', title: '无人机围界巡检报告', period: '2026-W25', status: 'draft', format: 'HTML', generatedAt: '-', downloadUrl: '/reports/flight-week-25.html' },
  { id: 'report-003', title: 'AI误报复核周报', period: '2026-W25', status: 'queued', format: 'Excel', generatedAt: '-', downloadUrl: '/reports/ai-review-week-25.xlsx' }
]

export const algorithmParameters: AlgorithmParameter[] = [
  { id: 'param-001', algorithmCode: 'smoke_fire', algorithmName: '烟火识别', threshold: 0.72, sensitivity: 86, enabled: true, updatedAt: '2026-06-16T09:00:00+08:00' },
  { id: 'param-002', algorithmCode: 'person_intrusion', algorithmName: '人员闯入', threshold: 0.68, sensitivity: 80, enabled: true, updatedAt: '2026-06-16T09:00:00+08:00' },
  { id: 'param-003', algorithmCode: 'helmet_detection', algorithmName: '安全帽识别', threshold: 0.74, sensitivity: 78, enabled: true, updatedAt: '2026-06-16T09:00:00+08:00' }
]

export const mapEvents: MapEvent[] = [
  { id: 'map-001', title: 'A区危化仓烟火识别', type: '烟火识别', severity: 'high', status: 'new', latitude: 31.2312, longitude: 121.4741, source: 'camera', detectedAt: '2026-06-16T09:18:00+08:00' },
  { id: 'map-002', title: 'B区围界人员闯入', type: '人员闯入', severity: 'medium', status: 'processing', latitude: 31.2306, longitude: 121.4729, source: 'camera', detectedAt: '2026-06-16T09:05:00+08:00' },
  { id: 'map-003', title: '东区无人机巡检异常点', type: '无人机巡检', severity: 'medium', status: 'new', latitude: 31.2321, longitude: 121.4752, source: 'drone', detectedAt: '2026-06-16T09:02:00+08:00' }
]

export const systemUsers: SystemUser[] = [
  {
    id: 'user-admin',
    username: 'admin',
    displayName: '系统管理员',
    organization: '示范园区运营中心',
    site: '一号工业园',
    status: 'active',
    roles: ['平台管理员'],
    permissions: ['*']
  },
  {
    id: 'user-operator',
    username: 'operator',
    displayName: '巡检值班员',
    organization: '示范园区运营中心',
    site: '一号工业园',
    status: 'active',
    roles: ['巡检值班员'],
    permissions: ['video:view', 'alarm:handle', 'task:dispatch']
  },
  {
    id: 'user-ai',
    username: 'ai.engineer',
    displayName: '算法工程师',
    organization: '示范园区运营中心',
    site: '一号工业园',
    status: 'active',
    roles: ['算法工程师'],
    permissions: ['dataset:manage', 'training:manage', 'model:publish']
  }
]

export const systemRoles: SystemRole[] = [
  { id: 'role-admin', code: 'platform_admin', name: '平台管理员', description: '平台配置、设备、模型发布和审计管理', permissions: ['*'], userCount: 1 },
  { id: 'role-operator', code: 'inspection_operator', name: '巡检值班员', description: '查看视频、处置告警、下发巡检任务', permissions: ['video:view', 'alarm:handle', 'task:dispatch'], userCount: 1 },
  { id: 'role-ai', code: 'ai_engineer', name: '算法工程师', description: '管理数据集、训练任务和模型版本', permissions: ['dataset:manage', 'training:manage', 'model:publish'], userCount: 1 }
]

export const auditLogs: AuditLog[] = [
  { id: 'audit-001', actor: 'system', action: 'BOOTSTRAP', targetType: 'platform', targetId: 'demo', result: 'success', createdAt: '2026-06-16T09:00:00+08:00' },
  { id: 'audit-002', actor: 'admin', action: 'LOGIN', targetType: 'user', targetId: 'user-admin', result: 'success', createdAt: '2026-06-16T09:05:00+08:00' },
  { id: 'audit-003', actor: 'system', action: 'MODEL_PUBLISH', targetType: 'model', targetId: 'model-002', result: 'success', createdAt: '2026-06-16T09:12:00+08:00' }
]

export const integrations: IntegrationConfig[] = [
  { id: 'int-camera-hik', name: '海康厂区视频 SDK', sourceType: 'camera', vendor: 'HIKVISION', sdkType: 'HIK_SDK', status: 'online', endpoint: 'https://hik-gateway.local', credentialRef: 'secret://integrations/hikvision', lastSyncAt: '2026-06-16T09:30:00+08:00', channelCount: 3 },
  { id: 'int-drone-dji', name: '大疆机场开放接口', sourceType: 'drone', vendor: 'DJI_DOCK', sdkType: 'DJI_CLOUD_API', status: 'online', endpoint: 'https://dji-dock.local', credentialRef: 'secret://integrations/dji', lastSyncAt: '2026-06-16T09:26:00+08:00', channelCount: 1 },
  { id: 'int-vehicle-jt1078', name: '车载 JT/T1078 网关', sourceType: 'vehicle', vendor: 'JT1078_GATEWAY', sdkType: 'JT1078_SDK', status: 'online', endpoint: 'https://jt1078-gateway.local', credentialRef: 'secret://integrations/jt1078', lastSyncAt: '2026-06-16T09:24:00+08:00', channelCount: 2 }
]

export const videoChannels: VideoChannel[] = [
  { id: 'ch-camera-a1', sourceId: 'dev-001', sourceType: 'camera', sourceName: 'A区危化仓-枪机01', name: '主码流', protocol: 'HIK_SDK', streamUrl: 'rtsp://example/live/a1', playUrl: 'http://localhost:8088/live/camera-a1.flv', aiEnabled: true, status: 'online', edgeNode: 'EDGE-01', latitude: 31.2312, longitude: 121.4741 },
  { id: 'ch-drone-001', sourceId: 'drone-001', sourceType: 'drone', sourceName: '一号机场无人机', name: '无人机直播', protocol: 'DJI_SDK', streamUrl: 'rtsp://example/live/drone-001', playUrl: 'http://localhost:8088/live/drone-001.flv', aiEnabled: true, status: 'online', edgeNode: 'EDGE-01', latitude: 31.2321, longitude: 121.4752 },
  { id: 'ch-vehicle-front', sourceId: 'vehicle-001', sourceType: 'vehicle', sourceName: '危化品转运车01', name: '前向摄像头', protocol: 'JT1078', streamUrl: 'jt1078://vehicle-001/front', playUrl: 'http://localhost:8088/live/vehicle-front.flv', aiEnabled: true, status: 'online', edgeNode: 'EDGE-01', latitude: 31.2298, longitude: 121.4718 },
  { id: 'ch-vehicle-cargo', sourceId: 'vehicle-001', sourceType: 'vehicle', sourceName: '危化品转运车01', name: '车厢摄像头', protocol: 'JT1078', streamUrl: 'jt1078://vehicle-001/cargo', playUrl: 'http://localhost:8088/live/vehicle-cargo.flv', aiEnabled: true, status: 'online', edgeNode: 'EDGE-01', latitude: 31.2298, longitude: 121.4718 }
]

export const drones: DroneAsset[] = [
  { id: 'drone-001', dockId: 'dock-001', name: '一号机场无人机', vendor: 'DJI_DOCK', status: 'ready', batteryPercent: 87, latitude: 31.2321, longitude: 121.4752, telemetry: ['ALT 86m', 'SPD 8.4m/s', 'HDG 126deg', 'LINK 98%'], activeTask: '东区围界固定航线' }
]

export const flightRoutes: FlightRoute[] = [
  { id: 'route-001', dockId: 'dock-001', name: '东区围界固定航线', waypointCount: 18, altitudeMeter: 86, status: 'ready' },
  { id: 'route-002', dockId: 'dock-001', name: '危化仓屋顶巡检', waypointCount: 12, altitudeMeter: 80, status: 'ready' }
]

export const flightTasks: FlightTask[] = [
  { id: 'flight-001', routeName: '东区围界固定航线', status: 'running', plannedAt: '2026-06-15T21:00:00+08:00' },
  { id: 'flight-002', routeName: '危化仓屋顶巡检', status: 'scheduled', plannedAt: '2026-06-16T09:30:00+08:00' }
]

export const vehicles: VehicleAsset[] = [
  { id: 'vehicle-001', plateNo: '沪A-D8123', name: '危化品转运车01', vendor: 'JT1078_GATEWAY', status: 'online', speedKph: 36.5, latitude: 31.2298, longitude: 121.4718, lastSeenAt: '2026-06-16T09:28:00+08:00' },
  { id: 'vehicle-002', plateNo: '沪A-F2639', name: '巡逻车02', vendor: 'JT1078_GATEWAY', status: 'online', speedKph: 18.2, latitude: 31.233, longitude: 121.476, lastSeenAt: '2026-06-16T09:27:00+08:00' }
]

export const vehicleTracks: VehicleTrackPoint[] = [
  { vehicleId: 'vehicle-001', latitude: 31.2298, longitude: 121.4718, speedKph: 36.5, heading: 86, sampledAt: '2026-06-16T09:20:00+08:00' },
  { vehicleId: 'vehicle-001', latitude: 31.2304, longitude: 121.4726, speedKph: 32, heading: 92, sampledAt: '2026-06-16T09:24:00+08:00' },
  { vehicleId: 'vehicle-001', latitude: 31.2311, longitude: 121.4734, speedKph: 28.2, heading: 104, sampledAt: '2026-06-16T09:28:00+08:00' }
]

export const aiTasks: AiTask[] = [
  { id: 'ai-task-001', sourceType: 'camera', channelId: 'ch-camera-a1', algorithmCode: 'smoke_fire', modelVersion: 'v1.4.2', status: 'running', confidence: 0.91, evidenceUrl: '/evidence/alarm-001.jpg', createdAt: '2026-06-16T09:18:00+08:00' },
  { id: 'ai-task-002', sourceType: 'drone', channelId: 'ch-drone-001', algorithmCode: 'person_intrusion', modelVersion: 'v1.2.0', status: 'running', confidence: 0.87, evidenceUrl: '/media/flight-001.mp4', createdAt: '2026-06-16T09:22:00+08:00' },
  { id: 'ai-task-003', sourceType: 'vehicle', channelId: 'ch-vehicle-front', algorithmCode: 'vehicle_parking', modelVersion: 'v0.8.0', status: 'running', confidence: 0.82, evidenceUrl: '/evidence/vehicle-001.jpg', createdAt: '2026-06-16T09:25:00+08:00' }
]
