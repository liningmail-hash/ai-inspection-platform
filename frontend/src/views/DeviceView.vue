<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  Activity, AlertCircle, Antenna, Cable, Camera, Cctv, ChevronDown, ChevronRight,
  Drone, Edit3, Gauge, MapPin, Network, PackageOpen, Plus, Radio,
  RefreshCw, Router, Satellite, Server, Signal, Trash2, Truck, Video, Wifi, Zap
} from '@lucide/vue'
import {
  createDroneDock, createNvr, createVehicle, deleteNvr, loadDroneDocks,
  loadNvrs, loadVehiclesV2, updateNvr,
  type ChannelNode, type DroneDockNode, type NvrDevice, type VehicleNode
} from '../services/api'

type Tab = 'nvr' | 'drone' | 'vehicle'
const activeTab = ref<Tab>('nvr')
const nvrs = ref<NvrDevice[]>([])
const drones = ref<DroneDockNode[]>([])
const vehicles = ref<VehicleNode[]>([])
const loading = ref(false)
const expandedNvrs = ref<Set<string>>(new Set())
const selectedChannel = ref<ChannelNode | null>(null)

// Modal state
const modalOpen = ref(false)
const modalMode = ref<'add' | 'edit'>('add')
const modalType = ref<Tab>('nvr')
const editingId = ref<string | null>(null)
const form = reactive({ name: '', gbDeviceId: '', gbPassword: '', sipHost: '', sipPort: 5060, vendor: '', location: '',
  dockId: '', droneEndpoint: '', plateNo: '', vehicleType: '', vehicleEndpoint: '' })
const submitting = ref(false)

const tabs: { key: Tab; label: string; icon: any }[] = [
  { key: 'nvr', label: 'NVR 设备', icon: Server },
  { key: 'drone', label: '无人机', icon: Drone },
  { key: 'vehicle', label: '车载终端', icon: Truck },
]

async function loadData() {
  loading.value = true
  try {
    const [n, d, v] = await Promise.all([loadNvrs(), loadDroneDocks(), loadVehiclesV2()])
    nvrs.value = n
    drones.value = d
    vehicles.value = v
  } catch { /* ignore */ } finally { loading.value = false }
}

function toggleNvr(id: string) {
  if (expandedNvrs.value.has(id)) expandedNvrs.value.delete(id)
  else expandedNvrs.value.add(id)
  expandedNvrs.value = new Set(expandedNvrs.value)
}

function openAdd(type: Tab) {
  modalMode.value = 'add'
  modalType.value = type
  editingId.value = null
  Object.assign(form, { name: '', gbDeviceId: '', gbPassword: '', sipHost: '', sipPort: 5060, vendor: '', location: '',
    dockId: '', droneEndpoint: '', plateNo: '', vehicleType: '', vehicleEndpoint: '' })
  modalOpen.value = true
}

function openEditNvr(nvr: NvrDevice) {
  modalMode.value = 'edit'
  modalType.value = 'nvr'
  editingId.value = nvr.id
  Object.assign(form, { name: nvr.name, gbDeviceId: nvr.gbDeviceId, gbPassword: '', sipHost: nvr.sipHost,
    sipPort: nvr.sipPort, vendor: nvr.vendor, location: nvr.location })
  modalOpen.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (modalType.value === 'nvr') {
      const payload: Record<string, unknown> = { name: form.name, gbDeviceId: form.gbDeviceId, gbPassword: form.gbPassword,
        sipHost: form.sipHost, sipPort: form.sipPort, vendor: form.vendor, location: form.location, edgeNodeId: 'EDGE-01' }
      if (modalMode.value === 'edit' && editingId.value) {
        await updateNvr(editingId.value, payload)
      } else {
        await createNvr(payload)
      }
    } else if (modalType.value === 'drone') {
      await createDroneDock({ name: form.name, dockId: form.dockId, vendor: 'DJI_DOCK', endpoint: form.droneEndpoint, edgeNodeId: 'EDGE-01' })
    } else if (modalType.value === 'vehicle') {
      await createVehicle({ name: form.name, plateNo: form.plateNo, vehicleType: form.vehicleType, vendor: 'JT1078', endpoint: form.vehicleEndpoint, edgeNodeId: 'EDGE-01' })
    }
    modalOpen.value = false
    await loadData()
  } catch { /* ignore */ } finally { submitting.value = false }
}

async function handleDeleteNvr(id: string) {
  if (!confirm('确认删除此 NVR 及其所有通道？')) return
  await deleteNvr(id)
  await loadData()
}

const statusColor = (s: string) => ({ online: '#22c55e', ready: '#3b82f6', offline: '#9ca3af', error: '#ef4444', deleted: '#6b7280' }[s] ?? '#9ca3af')
const statusText = (s: string) => ({ online: '在线', ready: '就绪', offline: '离线', error: '故障', deleted: '已删除' }[s] ?? s)

onMounted(loadData)
</script>

<template>
  <div class="device-manager">
    <!-- Header -->
    <div class="dm-header">
      <h2 class="dm-title">设备管理</h2>
      <span class="dm-sub">层级化管理 NVR / 无人机 / 车载终端及其通道</span>
    </div>

    <!-- Tabs -->
    <div class="dm-tabs">
      <button v-for="t in tabs" :key="t.key" :class="['dm-tab', { active: activeTab === t.key }]" @click="activeTab = t.key">
        <component :is="t.icon" :size="16" />
        <span>{{ t.label }}</span>
      </button>
      <div class="dm-tab-spacer" />
      <button class="dm-add-btn" @click="openAdd(activeTab)">
        <Plus :size="16" />
        <span>添加</span>
      </button>
    </div>

    <!-- Content -->
    <div class="dm-content">
      <!-- NVR Tab -->
      <template v-if="activeTab === 'nvr'">
        <div v-if="nvrs.length === 0 && !loading" class="dm-empty">
          <Server :size="48" opacity=".3" /><span>暂无 NVR 设备，点击"添加"接入第一台 NVR</span>
        </div>
        <div v-for="nvr in nvrs" :key="nvr.id" class="dm-nvr-card">
          <div class="dm-nvr-header" @click="toggleNvr(nvr.id)">
            <component :is="expandedNvrs.has(nvr.id) ? ChevronDown : ChevronRight" :size="18" class="dm-expand" />
            <div class="dm-nvr-info">
              <span class="dm-nvr-name">{{ nvr.name }}</span>
              <span class="dm-nvr-meta">{{ nvr.vendor || 'Unknown' }} · {{ nvr.gbDeviceId || '未配置国标ID' }}</span>
            </div>
            <div class="dm-nvr-right">
              <span :class="['dm-badge', nvr.status]">{{ statusText(nvr.status) }}</span>
              <span class="dm-channel-count">{{ nvr.channelCount }} 通道</span>
              <div class="dm-actions">
                <button class="dm-icon-btn" title="编辑" @click.stop="openEditNvr(nvr)"><Edit3 :size="14" /></button>
                <button class="dm-icon-btn danger" title="删除" @click.stop="handleDeleteNvr(nvr.id)"><Trash2 :size="14" /></button>
              </div>
            </div>
          </div>
          <!-- Channels -->
          <div v-if="expandedNvrs.has(nvr.id)" class="dm-channels">
            <div v-if="nvr.channels.length === 0" class="dm-empty-sm">暂无通道 · 需通过 GB/T 28181 同步</div>
            <div v-for="ch in nvr.channels" :key="ch.id" :class="['dm-channel-row', { selected: selectedChannel?.id === ch.id }]" @click="selectedChannel = ch">
              <div class="dm-ch-left">
                <Camera :size="15" />
                <div>
                  <div class="dm-ch-name">{{ ch.channelName || ('通道 ' + (ch.channelNo + 1)) }}</div>
                  <div class="dm-ch-meta">CH{{ String(ch.channelNo).padStart(2, '0') }} · {{ ch.ptzType === 'ptz' ? '云台' : '固定' }}</div>
                </div>
              </div>
              <div class="dm-ch-right">
                <span v-if="ch.aiEnabled" class="dm-ai-tag">AI</span>
                <span :class="['dm-badge small', ch.status]">{{ statusText(ch.status) }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- Drone Tab -->
      <template v-if="activeTab === 'drone'">
        <div v-if="drones.length === 0 && !loading" class="dm-empty">
          <Drone :size="48" opacity=".3" /><span>暂无无人机机场</span>
        </div>
        <div class="dm-cards">
          <div v-for="d in drones" :key="d.id" class="dm-card">
            <div class="dm-card-top">
              <Drone :size="20" /><span class="dm-card-name">{{ d.name }}</span>
              <span :class="['dm-badge', d.status]">{{ statusText(d.status) }}</span>
            </div>
            <div class="dm-card-body">
              <div><MapPin :size="13" />{{ d.location || '未设置' }}</div>
              <div><Gauge :size="13" />电量 {{ d.batteryPercent }}%</div>
              <div><Activity :size="13" />{{ d.weather || '-' }}</div>
              <div><Router :size="13" />{{ d.droneCount }} 架无人机</div>
            </div>
          </div>
        </div>
      </template>

      <!-- Vehicle Tab -->
      <template v-if="activeTab === 'vehicle'">
        <div v-if="vehicles.length === 0 && !loading" class="dm-empty">
          <Truck :size="48" opacity=".3" /><span>暂无车载终端</span>
        </div>
        <div class="dm-cards">
          <div v-for="v in vehicles" :key="v.id" class="dm-card">
            <div class="dm-card-top">
              <Truck :size="20" /><span class="dm-card-name">{{ v.plateNo || v.name }}</span>
              <span :class="['dm-badge', v.status]">{{ statusText(v.status) }}</span>
            </div>
            <div class="dm-card-body">
              <div><MapPin :size="13" />{{ v.latitude.toFixed(4) }}, {{ v.longitude.toFixed(4) }}</div>
              <div><Gauge :size="13" />{{ v.speedKph }} km/h</div>
              <div><Cable :size="13" />{{ v.channelCount }} 通道</div>
              <div><Radio :size="13" />{{ v.protocol }}</div>
            </div>
            <!-- Vehicle channels -->
            <div v-if="v.channels.length" class="dm-card-channels">
              <div v-for="ch in v.channels" :key="ch.id" class="dm-card-ch-row">
                <Camera :size="13" /><span>{{ ch.channelName }}</span>
                <span :class="['dm-badge small', ch.status]">{{ statusText(ch.status) }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Add/Edit Modal -->
    <Teleport to="body">
      <div v-if="modalOpen" class="dm-overlay" @click.self="modalOpen = false">
        <div class="dm-modal">
          <h3>{{ modalMode === 'add' ? '添加' : '编辑' }} {{ tabs.find(t => t.key === modalType)?.label }}</h3>
          <form @submit.prevent="handleSubmit" class="dm-form">
            <label>名称 <input v-model="form.name" required /></label>
            <template v-if="modalType === 'nvr'">
              <label>国标设备 ID (20位) <input v-model="form.gbDeviceId" placeholder="34020000001320000001" /></label>
              <label>注册密码 <input v-model="form.gbPassword" type="password" placeholder="NVR 配置的密码" /></label>
              <div class="dm-row">
                <label style="flex:1">SIP 服务器 IP <input v-model="form.sipHost" placeholder="192.168.1.100" /></label>
                <label style="width:80px">端口 <input v-model.number="form.sipPort" type="number" /></label>
              </div>
              <label>厂商 <input v-model="form.vendor" placeholder="HIKVISION / DAHUA" /></label>
              <label>位置 <input v-model="form.location" placeholder="机房/楼层" /></label>
            </template>
            <template v-if="modalType === 'drone'">
              <label>机场编号 <input v-model="form.dockId" placeholder="dock-001" /></label>
              <label>API 端点 <input v-model="form.droneEndpoint" placeholder="https://dji-dock.local" /></label>
            </template>
            <template v-if="modalType === 'vehicle'">
              <label>车牌号 <input v-model="form.plateNo" placeholder="沪A-XXXXX" /></label>
              <label>车辆类型 <input v-model="form.vehicleType" placeholder="危化品转运车" /></label>
              <label>JT1078 端点 <input v-model="form.vehicleEndpoint" placeholder="https://jt1078-gateway.local" /></label>
            </template>
            <div class="dm-modal-actions">
              <button type="button" class="dm-btn-cancel" @click="modalOpen = false">取消</button>
              <button type="submit" class="dm-btn-submit" :disabled="submitting">{{ submitting ? '保存中...' : '保存' }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.device-manager { display: flex; flex-direction: column; height: 100%; gap: 0; }
.dm-header { padding: 24px 24px 0; }
.dm-title { font-size: 18px; font-weight: 600; margin: 0 0 4px; }
.dm-sub { font-size: 13px; color: var(--color-text-muted, #6b7280); }

.dm-tabs { display: flex; gap: 6px; padding: 16px 24px 12px; border-bottom: 1px solid var(--color-border, #e5e7eb); }
.dm-tab { display: flex; align-items: center; gap: 6px; padding: 7px 14px; border: 1px solid var(--color-border, #d1d5db); border-radius: 6px; background: #fff; font-size: 13px; cursor: pointer; color: #374151; }
.dm-tab.active { background: var(--color-primary, #3b82f6); color: #fff; border-color: var(--color-primary, #3b82f6); }
.dm-tab-spacer { flex: 1; }
.dm-add-btn { display: flex; align-items: center; gap: 5px; padding: 7px 14px; border: none; border-radius: 6px; background: var(--color-primary, #3b82f6); color: #fff; font-size: 13px; cursor: pointer; }

.dm-content { flex: 1; overflow-y: auto; padding: 16px 24px; }
.dm-empty { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 60px 0; color: #9ca3af; font-size: 14px; }
.dm-empty-sm { text-align: center; padding: 20px; color: #9ca3af; font-size: 13px; }

.dm-nvr-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; margin-bottom: 8px; overflow: hidden; }
.dm-nvr-header { display: flex; align-items: center; gap: 10px; padding: 14px 16px; cursor: pointer; user-select: none; }
.dm-nvr-header:hover { background: #f9fafb; }
.dm-expand { color: #6b7280; flex-shrink: 0; }
.dm-nvr-info { flex: 1; min-width: 0; }
.dm-nvr-name { font-size: 14px; font-weight: 600; display: block; }
.dm-nvr-meta { font-size: 12px; color: #6b7280; }
.dm-nvr-right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.dm-channel-count { font-size: 12px; color: #9ca3af; }
.dm-actions { display: flex; gap: 4px; }
.dm-icon-btn { display: flex; align-items: center; padding: 6px; border: 1px solid #e5e7eb; border-radius: 5px; background: #fff; cursor: pointer; color: #374151; }
.dm-icon-btn.danger { color: #ef4444; border-color: #fecaca; }
.dm-icon-btn:hover { background: #f3f4f6; }

.dm-badge { display: inline-flex; align-items: center; padding: 2px 8px; border-radius: 10px; font-size: 11px; font-weight: 500; }
.dm-badge.online { background: #dcfce7; color: #16a34a; }
.dm-badge.ready { background: #dbeafe; color: #2563eb; }
.dm-badge.offline { background: #f3f4f6; color: #6b7280; }
.dm-badge.error { background: #fef2f2; color: #dc2626; }
.dm-badge.small { padding: 1px 6px; font-size: 11px; }

.dm-channels { border-top: 1px solid #f3f4f6; }
.dm-channel-row { display: flex; align-items: center; justify-content: space-between; padding: 10px 16px 10px 44px; cursor: pointer; }
.dm-channel-row:hover { background: #f0f9ff; }
.dm-channel-row.selected { background: #eff6ff; }
.dm-ch-left { display: flex; align-items: center; gap: 10px; }
.dm-ch-name { font-size: 13px; font-weight: 500; }
.dm-ch-meta { font-size: 11px; color: #9ca3af; }
.dm-ch-right { display: flex; align-items: center; gap: 8px; }
.dm-ai-tag { display: inline-flex; align-items: center; padding: 1px 5px; background: #fef3c7; color: #b45309; border-radius: 3px; font-size: 10px; font-weight: 600; letter-spacing: .5px; }

.dm-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 12px; }
.dm-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; }
.dm-card-top { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.dm-card-name { font-weight: 600; font-size: 14px; flex: 1; }
.dm-card-body { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; font-size: 12px; color: #6b7280; }
.dm-card-body > div { display: flex; align-items: center; gap: 4px; }
.dm-card-channels { margin-top: 10px; border-top: 1px solid #f3f4f6; padding-top: 8px; }
.dm-card-ch-row { display: flex; align-items: center; gap: 6px; font-size: 12px; padding: 3px 0; }

.dm-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.35); display: flex; align-items: center; justify-content: center; z-index: 100; }
.dm-modal { background: #fff; border-radius: 10px; padding: 24px; width: 440px; max-height: 80vh; overflow-y: auto; box-shadow: 0 20px 60px rgba(0,0,0,.15); }
.dm-modal h3 { margin: 0 0 18px; font-size: 16px; }
.dm-form { display: flex; flex-direction: column; gap: 12px; }
.dm-form label { font-size: 13px; color: #374151; display: flex; flex-direction: column; gap: 4px; }
.dm-form input { padding: 8px 10px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 13px; }
.dm-form input:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 2px rgba(59,130,246,.15); }
.dm-row { display: flex; gap: 10px; }
.dm-modal-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 6px; }
.dm-btn-cancel { padding: 8px 16px; border: 1px solid #d1d5db; border-radius: 6px; background: #fff; font-size: 13px; cursor: pointer; }
.dm-btn-submit { padding: 8px 16px; border: none; border-radius: 6px; background: #3b82f6; color: #fff; font-size: 13px; cursor: pointer; }
.dm-btn-submit:disabled { opacity: .6; cursor: not-allowed; }
</style>
