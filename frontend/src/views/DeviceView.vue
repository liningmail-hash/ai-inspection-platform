<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { Edit3, PlugZap, Plus, RefreshCw, Trash2, X } from '@lucide/vue'
import {
  createDevice,
  deleteDevice,
  syncDeviceChannels,
  testDevice,
  updateDevice,
  type Device,
  type DevicePayload,
  type IntegrationConfig,
  type VehicleAsset,
  type VideoChannel
} from '../services/api'

const props = defineProps<{
  devices: Device[]
  integrations: IntegrationConfig[]
  videoChannels: VideoChannel[]
  vehicles: VehicleAsset[]
}>()

const sourceOptions: DevicePayload['sourceType'][] = ['camera', 'drone', 'vehicle']
const vendorOptions = ['MOCK_VENDOR', 'HIKVISION', 'DAHUA', 'DJI_DOCK', 'JT1078']

const localDevices = ref<Device[]>([])
const modalOpen = ref(false)
const editingDeviceId = ref<string | null>(null)
const pendingDelete = ref<Device | null>(null)
const deviceStatus = ref<Record<string, { tone: string; text: string }>>({})
const pageStatus = ref({ tone: 'muted', text: '设备管理已切换为真实联调，保存结果以接口返回为准' })
const form = reactive<DevicePayload>({
  name: '',
  sourceType: 'camera',
  vendor: 'MOCK_VENDOR',
  endpoint: '',
  credentialRef: '',
  location: '',
  edgeNodeId: 'EDGE-01'
})

watch(
  () => props.devices,
  (devices) => {
    localDevices.value = devices.map((device) => ({ ...device }))
  },
  { immediate: true }
)

const sourceText: Record<string, string> = {
  camera: '厂区摄像头',
  drone: '无人机',
  vehicle: '车载终端'
}

const currentMode = computed(() => (editingDeviceId.value ? '编辑设备' : '添加设备'))

function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : '接口调用失败'
}

function resetForm() {
  form.name = ''
  form.sourceType = 'camera'
  form.vendor = 'MOCK_VENDOR'
  form.endpoint = ''
  form.credentialRef = ''
  form.location = ''
  form.edgeNodeId = 'EDGE-01'
}

function openCreateModal() {
  editingDeviceId.value = null
  resetForm()
  modalOpen.value = true
}

function openEditModal(device: Device) {
  editingDeviceId.value = device.id ?? null
  form.name = device.name
  form.sourceType = device.sourceType ?? 'camera'
  form.vendor = device.vendor
  form.endpoint = device.endpoint ?? ''
  form.credentialRef = device.credentialRef ?? ''
  form.location = device.location ?? ''
  form.edgeNodeId = device.edgeNodeId ?? 'EDGE-01'
  modalOpen.value = true
}

function applySavedDevice(saved: unknown, fallback: DevicePayload, id?: string): Device {
  const payload = saved && typeof saved === 'object' ? (saved as Partial<Device>) : {}
  return {
    id: payload.id ?? id,
    name: payload.name ?? fallback.name,
    sourceType: payload.sourceType ?? fallback.sourceType,
    vendor: payload.vendor ?? fallback.vendor,
    protocol: payload.protocol ?? fallback.vendor,
    endpoint: payload.endpoint ?? fallback.endpoint,
    credentialRef: payload.credentialRef ?? fallback.credentialRef,
    location: payload.location ?? fallback.location,
    edgeNodeId: payload.edgeNodeId ?? fallback.edgeNodeId,
    status: payload.status ?? 'unknown',
    bitrate: payload.bitrate ?? '-',
    latency: payload.latency ?? '-',
    createdAt: payload.createdAt,
    updatedAt: payload.updatedAt
  }
}

async function submitDevice() {
  const payload = { ...form }
  pageStatus.value = { tone: 'warning', text: `${currentMode.value}提交中` }
  try {
    if (editingDeviceId.value) {
      const saved = await updateDevice(editingDeviceId.value, payload)
      const nextDevice = applySavedDevice(saved, payload, editingDeviceId.value)
      localDevices.value = localDevices.value.map((device) => (device.id === editingDeviceId.value ? nextDevice : device))
      pageStatus.value = { tone: 'success', text: `编辑成功：${nextDevice.name}` }
    } else {
      const saved = await createDevice(payload)
      const nextDevice = applySavedDevice(saved, payload)
      localDevices.value = [nextDevice, ...localDevices.value]
      pageStatus.value = { tone: 'success', text: `添加成功：${nextDevice.name}` }
    }
    modalOpen.value = false
  } catch (error) {
  pageStatus.value = { tone: 'danger', text: `${currentMode.value}失败：${errorMessage(error)}。请检查后端接口返回。` }
  }
}

async function confirmDelete() {
  const device = pendingDelete.value
  if (!device?.id) return
  pageStatus.value = { tone: 'warning', text: `删除执行中：${device.name}` }
  try {
    await deleteDevice(device.id)
    localDevices.value = localDevices.value.filter((item) => item.id !== device.id)
    pageStatus.value = { tone: 'success', text: `删除成功：${device.name}` }
    pendingDelete.value = null
  } catch (error) {
    pageStatus.value = { tone: 'danger', text: `删除失败：${errorMessage(error)}` }
  }
}

async function runDeviceAction(device: Device, label: string, action: (id: string) => Promise<unknown>) {
  if (!device.id) return
  deviceStatus.value[device.id] = { tone: 'warning', text: `${label}执行中` }
  try {
    const result = await action(device.id)
    const message = result && typeof result === 'object' && 'message' in result ? String((result as { message: unknown }).message) : `${label}成功`
    deviceStatus.value[device.id] = { tone: 'success', text: message }
  } catch (error) {
    deviceStatus.value[device.id] = { tone: 'danger', text: `${label}失败：${errorMessage(error)}` }
  }
}
</script>

<template>
  <section class="page-grid">
    <div class="toolbar">
      <input class="input" placeholder="设备名称 / endpoint / 厂商" />
      <button class="primary-btn" @click="openCreateModal"><Plus :size="15" /> 接入设备</button>
    </div>
    <div class="operation-result" :class="pageStatus.tone">{{ pageStatus.text }}</div>

    <article class="panel">
      <h2 class="panel-title">设备真实操作台 <span class="badge warning">保存依赖后端接口</span></h2>
      <table class="table device-table">
        <thead>
          <tr>
            <th>设备</th>
            <th>类型</th>
            <th>厂商</th>
            <th>endpoint</th>
            <th>位置 / 边缘</th>
            <th>状态</th>
            <th>操作</th>
            <th>反馈</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="device in localDevices" :key="device.id ?? device.name">
            <td>{{ device.name }}</td>
            <td>{{ sourceText[device.sourceType ?? 'camera'] }}</td>
            <td>{{ device.vendor }}</td>
            <td class="truncate-cell">{{ device.endpoint ?? '-' }}</td>
            <td>{{ device.location ?? '-' }} / {{ device.edgeNodeId ?? '-' }}</td>
            <td><span class="badge" :class="device.status === 'online' ? 'success' : 'warning'">{{ device.status }}</span></td>
            <td>
              <div class="row-actions">
                <button class="ghost icon-btn" title="编辑设备" @click="openEditModal(device)"><Edit3 :size="14" /></button>
                <button class="ghost icon-btn" title="测试连接" @click="runDeviceAction(device, '测试连接', testDevice)"><PlugZap :size="14" /></button>
                <button class="ghost icon-btn" title="同步通道" @click="runDeviceAction(device, '同步通道', syncDeviceChannels)"><RefreshCw :size="14" /></button>
                <button class="ghost icon-btn" title="删除设备" @click="pendingDelete = device"><Trash2 :size="14" /></button>
              </div>
            </td>
            <td>
              <div class="operation-result compact" :class="deviceStatus[device.id ?? '']?.tone ?? 'muted'">
                {{ deviceStatus[device.id ?? '']?.text ?? '等待操作' }}
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </article>

    <div class="split">
      <article class="panel">
        <h2 class="panel-title">统一视频通道</h2>
        <table class="table">
          <thead><tr><th>来源</th><th>通道</th><th>协议</th><th>AI</th><th>节点</th></tr></thead>
          <tbody>
            <tr v-for="channel in videoChannels" :key="channel.id">
              <td>{{ channel.sourceType }} / {{ channel.sourceName }}</td>
              <td>{{ channel.name }}</td>
              <td>{{ channel.protocol }}</td>
              <td><span class="badge" :class="channel.aiEnabled ? 'success' : 'warning'">{{ channel.aiEnabled ? '启用' : '停用' }}</span></td>
              <td>{{ channel.edgeNode }}</td>
            </tr>
          </tbody>
        </table>
      </article>
      <article class="panel">
        <h2 class="panel-title">厂家 SDK 集成</h2>
        <div class="metric-list">
          <div v-for="item in integrations" :key="item.id" class="metric-row">
            <span>{{ item.name }}</span>
            <strong>{{ item.channelCount }} 路</strong>
          </div>
        </div>
      </article>
    </div>

    <div v-if="modalOpen" class="modal-backdrop">
      <form class="modal-panel" @submit.prevent="submitDevice">
        <div class="panel-title">
          <span>{{ currentMode }}</span>
          <button class="ghost icon-btn" type="button" title="关闭" @click="modalOpen = false"><X :size="15" /></button>
        </div>
        <div class="form-grid">
          <label>设备名称<input v-model.trim="form.name" class="input" required /></label>
          <label>来源类型<select v-model="form.sourceType" class="input"><option v-for="item in sourceOptions" :key="item" :value="item">{{ item }}</option></select></label>
          <label>厂商<select v-model="form.vendor" class="input"><option v-for="item in vendorOptions" :key="item" :value="item">{{ item }}</option></select></label>
          <label>endpoint<input v-model.trim="form.endpoint" class="input" required /></label>
          <label>credentialRef<input v-model.trim="form.credentialRef" class="input" required /></label>
          <label>location<input v-model.trim="form.location" class="input" required /></label>
          <label>edgeNodeId<input v-model.trim="form.edgeNodeId" class="input" required /></label>
        </div>
        <div class="toolbar modal-actions">
          <button class="primary-btn" type="submit">{{ editingDeviceId ? '保存修改' : '提交新增' }}</button>
          <button class="ghost" type="button" @click="modalOpen = false">取消</button>
        </div>
      </form>
    </div>

    <div v-if="pendingDelete" class="modal-backdrop">
      <div class="modal-panel confirm-panel">
        <h2 class="panel-title">删除确认</h2>
        <p class="muted">确认删除 {{ pendingDelete.name }}？删除是否软删由后端接口决定。</p>
        <div class="toolbar modal-actions">
          <button class="primary-btn" @click="confirmDelete">确认删除</button>
          <button class="ghost" @click="pendingDelete = null">取消</button>
        </div>
      </div>
    </div>
  </section>
</template>
