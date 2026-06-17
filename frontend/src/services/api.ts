import {
  alarms as mockAlarms,
  algorithmParameters as mockAlgorithmParameters,
  devices as mockDevices,
  drone as mockDrone,
  auditLogs as mockAuditLogs,
  aiTasks as mockAiTasks,
  inspectionReports as mockInspectionReports,
  inspectionTasks as mockInspectionTasks,
  integrations as mockIntegrations,
  kpis as mockKpis,
  mapEvents as mockMapEvents,
  mediaAssets as mockMediaAssets,
  models as mockModels,
  plans as mockPlans,
  drones as mockDrones,
  flightRoutes as mockFlightRoutes,
  flightTasks as mockFlightTasks,
  systemRoles as mockSystemRoles,
  systemUsers as mockSystemUsers,
  vehicles as mockVehicles,
  vehicleTracks as mockVehicleTracks,
  videoChannels as mockVideoChannels,
  type Alarm,
  type AiTask,
  type AlgorithmParameter,
  type AuditLog,
  type Device,
  type DroneAsset,
  type DroneSummary,
  type FlightRoute,
  type FlightTask,
  type InspectionPlan,
  type InspectionReport,
  type InspectionTask,
  type IntegrationConfig,
  type Kpi,
  type MapEvent,
  type MediaAsset,
  type Model,
  type SystemRole,
  type SystemUser,
  type VehicleAsset,
  type VehicleTrackPoint,
  type VideoChannel,
  type VideoSession
} from '../data/mock'

function resolveApiBaseUrl() {
  const configuredUrl = import.meta.env.VITE_API_BASE_URL
  if (configuredUrl) return configuredUrl
  if (typeof window !== 'undefined' && window.location.hostname && !['localhost', '127.0.0.1', '::1'].includes(window.location.hostname)) {
    return ''
  }
  return 'http://localhost:8080'
}

const API_BASE_URL = resolveApiBaseUrl()

type BackendKpi = Kpi
type BackendDevice = {
  id: string
  name: string
  sourceType?: 'camera' | 'drone' | 'vehicle'
  vendor: string
  protocol?: string
  endpoint?: string
  credentialRef?: string
  location?: string
  edgeNodeId?: string
  status: string
  createdAt?: string
  updatedAt?: string
  streamUrl?: string | null
}
type BackendPlan = {
  id: string
  name: string
  pointCount: number
  algorithm: string
  schedule: string
  status: string
}
type BackendAlarm = {
  id: string
  level: string
  type: string
  device: string
  status: string
  detectedAt: string
  evidenceUrl?: string
}
type BackendModel = {
  id: string
  algorithm: string
  version: string
  status: string
  metrics: Record<string, string | number>
}
type BackendOverview = {
  kpis: BackendKpi[]
  alarms: BackendAlarm[]
  plans: BackendPlan[]
  dock: {
    id: string
    name: string
    status: string
    batteryPercent: number
    weather: string
  }
}

type BackendUser = SystemUser
type BackendRole = SystemRole
type BackendAuditLog = AuditLog
type BackendInspectionTask = InspectionTask
type BackendMediaAsset = MediaAsset
type BackendInspectionReport = InspectionReport
type BackendAlgorithmParameter = AlgorithmParameter
type BackendMapEvent = MapEvent
type BackendIntegration = IntegrationConfig
type BackendVideoChannel = VideoChannel
type BackendDroneAsset = DroneAsset
type BackendFlightRoute = FlightRoute
type BackendFlightTask = FlightTask
type BackendVehicle = VehicleAsset
type BackendVehicleTrack = VehicleTrackPoint
type BackendAiTask = AiTask

type DevicePayload = {
  name: string
  sourceType: 'camera' | 'drone' | 'vehicle'
  vendor: string
  endpoint: string
  credentialRef: string
  location: string
  edgeNodeId: string
}

type AppData = {
  kpis: Kpi[]
  alarms: Alarm[]
  devices: Device[]
  plans: InspectionPlan[]
  models: Model[]
  drone: DroneSummary
  inspectionTasks: InspectionTask[]
  mediaAssets: MediaAsset[]
  inspectionReports: InspectionReport[]
  algorithmParameters: AlgorithmParameter[]
  mapEvents: MapEvent[]
  systemUsers: SystemUser[]
  systemRoles: SystemRole[]
  auditLogs: AuditLog[]
  integrations: IntegrationConfig[]
  videoChannels: VideoChannel[]
  drones: DroneAsset[]
  flightRoutes: FlightRoute[]
  flightTasks: FlightTask[]
  vehicles: VehicleAsset[]
  vehicleTracks: VehicleTrackPoint[]
  aiTasks: AiTask[]
  source: 'api' | 'mock'
}

const levelText: Record<string, string> = {
  high: '高',
  medium: '中',
  low: '低'
}

const alarmStatusText: Record<string, string> = {
  new: '待确认',
  processing: '处理中',
  false_positive: '误报',
  dispatched: '已派单',
  closed: '已关闭'
}

const planStatusText: Record<string, string> = {
  running: '运行中',
  enabled: '运行中',
  paused: '暂停',
  disabled: '停用'
}

const modelStatusText: Record<string, string> = {
  production: '生产',
  canary: '灰度',
  candidate: '候选',
  archived: '归档'
}

const vendorText: Record<string, string> = {
  HIKVISION: '海康',
  DAHUA: '大华',
  GENERIC: '通用',
  JT1078: '部标'
}

async function requestJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { Accept: 'application/json' }
  })
  if (!response.ok) throw new Error(`${path} failed: ${response.status}`)
  return response.json() as Promise<T>
}

function formatTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).format(date)
}

function mapAlarm(alarm: BackendAlarm): Alarm {
  return {
    id: alarm.id,
    level: levelText[alarm.level] ?? alarm.level,
    type: alarm.type,
    device: alarm.device,
    status: alarmStatusText[alarm.status] ?? alarm.status,
    time: formatTime(alarm.detectedAt),
    evidenceUrl: alarm.evidenceUrl
  }
}

function mapPlan(plan: BackendPlan): InspectionPlan {
  return {
    id: plan.id,
    name: plan.name,
    points: plan.pointCount,
    algorithm: plan.algorithm,
    schedule: plan.schedule,
    status: planStatusText[plan.status] ?? plan.status
  }
}

function mapDevice(device: BackendDevice): Device {
  return {
    id: device.id,
    name: device.name,
    sourceType: device.sourceType ?? 'camera',
    vendor: vendorText[device.vendor] ?? device.vendor,
    protocol: device.protocol ?? device.vendor,
    endpoint: device.endpoint ?? device.streamUrl ?? '',
    credentialRef: device.credentialRef ?? '',
    location: device.location ?? '',
    edgeNodeId: device.edgeNodeId ?? '',
    createdAt: device.createdAt,
    updatedAt: device.updatedAt,
    status: device.status,
    bitrate: device.status === 'online' ? 'API' : '-',
    latency: device.status === 'online' ? 'API' : '-'
  }
}

function mapModel(model: BackendModel): Model {
  return {
    id: model.id,
    algorithm: model.algorithm,
    version: model.version,
    precision: String(model.metrics.precision ?? '-'),
    recall: String(model.metrics.recall ?? '-'),
    status: modelStatusText[model.status] ?? model.status
  }
}

function fallbackData(): AppData {
  return {
    kpis: mockKpis,
    alarms: mockAlarms,
    devices: mockDevices,
    plans: mockPlans,
    models: mockModels,
    drone: mockDrone,
    inspectionTasks: mockInspectionTasks,
    mediaAssets: mockMediaAssets,
    inspectionReports: mockInspectionReports,
    algorithmParameters: mockAlgorithmParameters,
    mapEvents: mockMapEvents,
    systemUsers: mockSystemUsers,
    systemRoles: mockSystemRoles,
    auditLogs: mockAuditLogs,
    integrations: mockIntegrations,
    videoChannels: mockVideoChannels,
    drones: mockDrones,
    flightRoutes: mockFlightRoutes,
    flightTasks: mockFlightTasks,
    vehicles: mockVehicles,
    vehicleTracks: mockVehicleTracks,
    aiTasks: mockAiTasks,
    source: 'mock'
  }
}

export async function loadAppData(): Promise<AppData> {
  try {
    const [
      overview,
      devices,
      models,
      inspectionTasks,
      mediaAssets,
      inspectionReports,
      algorithmParameters,
      mapEvents,
      systemUsers,
      systemRoles,
      auditLogs,
      integrations,
      videoChannels,
      drones,
      flightRoutes,
      flightTasks,
      vehicles,
      vehicleTracks,
      aiTasks
    ] = await Promise.all([
      requestJson<BackendOverview>('/api/overview'),
      requestJson<BackendDevice[]>('/api/devices'),
      requestJson<BackendModel[]>('/api/models'),
      requestJson<BackendInspectionTask[]>('/api/inspection-tasks'),
      requestJson<BackendMediaAsset[]>('/api/media-assets'),
      requestJson<BackendInspectionReport[]>('/api/inspection-reports'),
      requestJson<BackendAlgorithmParameter[]>('/api/algorithm-parameters'),
      requestJson<BackendMapEvent[]>('/api/map-events'),
      requestJson<BackendUser[]>('/api/system/users'),
      requestJson<BackendRole[]>('/api/system/roles'),
      requestJson<BackendAuditLog[]>('/api/system/audit-logs'),
      requestJson<BackendIntegration[]>('/api/integrations'),
      requestJson<BackendVideoChannel[]>('/api/video/channels'),
      requestJson<BackendDroneAsset[]>('/api/drones'),
      requestJson<BackendFlightRoute[]>('/api/flight-routes'),
      requestJson<BackendFlightTask[]>('/api/flight-tasks'),
      requestJson<BackendVehicle[]>('/api/vehicles'),
      requestJson<BackendVehicleTrack[]>('/api/vehicles/a3000000-0000-0000-0000-000000000001/tracks').catch(() => mockVehicleTracks),
      requestJson<BackendAiTask[]>('/api/ai/tasks')
    ])

    return {
      kpis: overview.kpis,
      alarms: overview.alarms.map(mapAlarm),
      devices: devices.map(mapDevice),
      plans: overview.plans.map(mapPlan),
      models: models.map(mapModel),
      drone: {
        dock: overview.dock.name,
        status: overview.dock.status === 'ready' ? '就绪' : overview.dock.status,
        battery: overview.dock.batteryPercent,
        weather: overview.dock.weather,
        route: mockDrone.route,
        telemetry: mockDrone.telemetry
      },
      inspectionTasks,
      mediaAssets,
      inspectionReports,
      algorithmParameters,
      mapEvents,
      systemUsers,
      systemRoles,
      auditLogs,
      integrations,
      videoChannels,
      drones,
      flightRoutes,
      flightTasks,
      vehicles,
      vehicleTracks,
      aiTasks,
      source: 'api'
    }
  } catch {
    return fallbackData()
  }
}

export async function healthCheck() {
  return requestJson('/api/health')
}

export async function login(username: string, password: string) {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ username, password })
  })
  if (!response.ok) throw new Error(`login failed: ${response.status}`)
  return response.json()
}

export async function updateAlarmStatus(id: string, status: string) {
  const response = await fetch(`${API_BASE_URL}/api/alarms/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ status })
  })
  if (!response.ok) throw new Error(`update alarm failed: ${response.status}`)
  return response.json()
}

export async function publishModel(id: string, targetStatus: 'canary' | 'production' | 'archived') {
  const response = await fetch(`${API_BASE_URL}/api/models/${id}/publish`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ targetStatus, mode: targetStatus === 'archived' ? 'rollback' : 'manual' })
  })
  if (!response.ok) throw new Error(`publish model failed: ${response.status}`)
  return response.json()
}

export async function createInspectionTask(payload: { name: string; type: string; priority: string }) {
  const response = await fetch(`${API_BASE_URL}/api/inspection-tasks`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(payload)
  })
  if (!response.ok) throw new Error(`create inspection task failed: ${response.status}`)
  return response.json()
}

export async function createDevice(payload: DevicePayload) {
  const response = await fetch(`${API_BASE_URL}/api/devices`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(payload)
  })
  if (!response.ok) throw new Error(`create device failed: ${response.status}`)
  return response.json()
}

export async function updateDevice(id: string, payload: DevicePayload) {
  const response = await fetch(`${API_BASE_URL}/api/devices/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(payload)
  })
  if (!response.ok) throw new Error(`update device failed: ${response.status}`)
  return response.json()
}

export async function deleteDevice(id: string) {
  const response = await fetch(`${API_BASE_URL}/api/devices/${id}`, {
    method: 'DELETE',
    headers: { Accept: 'application/json' }
  })
  if (!response.ok) throw new Error(`delete device failed: ${response.status}`)
  return response.status === 204 ? null : response.json()
}

export async function testDevice(id: string) {
  const response = await fetch(`${API_BASE_URL}/api/devices/${id}/test`, {
    method: 'POST',
    headers: { Accept: 'application/json' }
  })
  if (!response.ok) throw new Error(`test device failed: ${response.status}`)
  return response.json()
}

export async function syncDeviceChannels(id: string) {
  const response = await fetch(`${API_BASE_URL}/api/devices/${id}/sync-channels`, {
    method: 'POST',
    headers: { Accept: 'application/json' }
  })
  if (!response.ok) throw new Error(`sync device channels failed: ${response.status}`)
  return response.json()
}

export async function testIntegration(id: string) {
  const response = await fetch(`${API_BASE_URL}/api/integrations/${id}/test`, {
    method: 'POST',
    headers: { Accept: 'application/json' }
  })
  if (!response.ok) throw new Error(`test integration failed: ${response.status}`)
  return response.json()
}

export async function syncIntegration(id: string) {
  const response = await fetch(`${API_BASE_URL}/api/integrations/${id}/sync`, {
    method: 'POST',
    headers: { Accept: 'application/json' }
  })
  if (!response.ok) throw new Error(`sync integration failed: ${response.status}`)
  return response.json()
}

export async function createVideoSession(channelId: string, protocol = 'webrtc'): Promise<VideoSession> {
  const response = await fetch(`${API_BASE_URL}/api/video/sessions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ channelId, protocol })
  })
  if (!response.ok) throw new Error(`create video session failed: ${response.status}`)
  return response.json()
}

export async function snapshotChannel(channelId: string) {
  const response = await fetch(`${API_BASE_URL}/api/video/channels/${channelId}/snapshot`, {
    method: 'POST',
    headers: { Accept: 'application/json' }
  })
  if (!response.ok) throw new Error(`snapshot failed: ${response.status}`)
  return response.json()
}

export async function recordChannel(channelId: string, seconds = 12) {
  const response = await fetch(`${API_BASE_URL}/api/video/channels/${channelId}/record`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ seconds })
  })
  if (!response.ok) throw new Error(`record failed: ${response.status}`)
  return response.json()
}

export async function createAiTask(payload: { channelId: string; algorithmCode: string; modelVersion: string }) {
  const response = await fetch(`${API_BASE_URL}/api/ai/tasks`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(payload)
  })
  if (!response.ok) throw new Error(`create ai task failed: ${response.status}`)
  return response.json()
}

export async function startFlightTask(id: string) {
  const response = await fetch(`${API_BASE_URL}/api/flight-tasks/${id}/start`, {
    method: 'POST',
    headers: { Accept: 'application/json' }
  })
  if (!response.ok) throw new Error(`start flight failed: ${response.status}`)
  return response.json()
}

export async function stopFlightTask(id: string) {
  const response = await fetch(`${API_BASE_URL}/api/flight-tasks/${id}/stop`, {
    method: 'POST',
    headers: { Accept: 'application/json' }
  })
  if (!response.ok) throw new Error(`stop flight failed: ${response.status}`)
  return response.json()
}

export { API_BASE_URL }
export type {
  Alarm,
  AiTask,
  AlgorithmParameter,
  AppData,
  AuditLog,
  Device,
  DevicePayload,
  DroneAsset,
  DroneSummary,
  FlightRoute,
  FlightTask,
  InspectionPlan,
  InspectionReport,
  InspectionTask,
  IntegrationConfig,
  Kpi,
  MapEvent,
  MediaAsset,
  Model,
  SystemRole,
  SystemUser,
  VehicleAsset,
  VehicleTrackPoint,
  VideoChannel,
  VideoSession
}
