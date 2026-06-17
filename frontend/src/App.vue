<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  Activity,
  AlertTriangle,
  BarChart3,
  Bot,
  Camera,
  Cpu,
  Database,
  Gauge,
  LineChart,
  Map,
  Plane,
  Settings,
  ShieldCheck,
  Video
} from '@lucide/vue'
import DashboardView from './views/DashboardView.vue'
import VideoWallView from './views/VideoWallView.vue'
import InspectionView from './views/InspectionView.vue'
import AlarmView from './views/AlarmView.vue'
import TrainingView from './views/TrainingView.vue'
import ModelView from './views/ModelView.vue'
import DroneView from './views/DroneView.vue'
import DeviceView from './views/DeviceView.vue'
import ReportsView from './views/ReportsView.vue'
import SettingsView from './views/SettingsView.vue'
import {
  createAiTask,
  createInspectionTask,
  createVideoSession,
  loadAppData,
  publishModel,
  recordChannel,
  snapshotChannel,
  startFlightTask,
  stopFlightTask,
  syncIntegration,
  testIntegration,
  updateAlarmStatus,
  type AppData
} from './services/api'
import {
  aiTasks,
  alarms,
  algorithmParameters,
  auditLogs,
  devices,
  drone,
  drones,
  flightRoutes,
  flightTasks,
  inspectionReports,
  inspectionTasks,
  integrations,
  kpis,
  mapEvents,
  mediaAssets,
  models,
  plans,
  systemRoles,
  systemUsers,
  vehicles,
  vehicleTracks,
  videoChannels
} from './data/mock'

const pages = [
  { key: 'dashboard', label: '驾驶舱', icon: Gauge, component: DashboardView },
  { key: 'video', label: '视频监控', icon: Video, component: VideoWallView },
  { key: 'inspection', label: 'AI巡检', icon: Bot, component: InspectionView },
  { key: 'alarm', label: '告警中心', icon: AlertTriangle, component: AlarmView },
  { key: 'training', label: '标注训练', icon: Database, component: TrainingView },
  { key: 'model', label: '模型管理', icon: Cpu, component: ModelView },
  { key: 'drone', label: '无人机巡检', icon: Plane, component: DroneView },
  { key: 'device', label: '设备管理', icon: Camera, component: DeviceView },
  { key: 'reports', label: '报表统计', icon: LineChart, component: ReportsView },
  { key: 'settings', label: '系统配置', icon: Settings, component: SettingsView }
]

const activeKey = ref('dashboard')
const activePage = computed(() => pages.find((page) => page.key === activeKey.value) ?? pages[0])
const appData = ref<AppData>({
  kpis,
  alarms,
  devices,
  plans,
  models,
  drone,
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
  source: 'mock'
})
const loading = ref(true)

onMounted(async () => {
  await refreshData()
  loading.value = false
})

async function refreshData() {
  appData.value = await loadAppData()
}

async function handleAlarmStatusChange(id: string, status: string) {
  try {
    await updateAlarmStatus(id, status)
    await refreshData()
  } catch (error) {
    console.warn('alarm status update skipped', error)
  }
}

async function handleModelPublish(id: string, status: 'canary' | 'production' | 'archived') {
  try {
    await publishModel(id, status)
    await refreshData()
  } catch (error) {
    console.warn('model publish skipped', error)
  }
}

async function handleTaskCreate() {
  try {
    await createInspectionTask({
      name: '即时巡检任务',
      type: 'immediate',
      priority: 'high'
    })
    await refreshData()
  } catch (error) {
    console.warn('inspection task creation skipped', error)
  }
}

async function handleIntegrationTest(id: string) {
  try {
    await testIntegration(id)
    await refreshData()
  } catch (error) {
    console.warn('integration test skipped', error)
  }
}

async function handleIntegrationSync(id: string) {
  try {
    await syncIntegration(id)
    await refreshData()
  } catch (error) {
    console.warn('integration sync skipped', error)
  }
}

async function handleVideoSession(channelId: string) {
  try {
    await createVideoSession(channelId)
    await refreshData()
  } catch (error) {
    console.warn('video session skipped', error)
  }
}

async function handleSnapshot(channelId: string) {
  try {
    await snapshotChannel(channelId)
    await refreshData()
  } catch (error) {
    console.warn('snapshot skipped', error)
  }
}

async function handleRecord(channelId: string) {
  try {
    await recordChannel(channelId)
    await refreshData()
  } catch (error) {
    console.warn('record skipped', error)
  }
}

async function handleAiTask(channelId: string) {
  try {
    await createAiTask({ channelId, algorithmCode: 'smoke_fire', modelVersion: 'production' })
    await refreshData()
  } catch (error) {
    console.warn('ai task skipped', error)
  }
}

async function handleFlightStart(id: string) {
  try {
    await startFlightTask(id)
    await refreshData()
  } catch (error) {
    console.warn('flight start skipped', error)
  }
}

async function handleFlightStop(id: string) {
  try {
    await stopFlightTask(id)
    await refreshData()
  } catch (error) {
    console.warn('flight stop skipped', error)
  }
}
</script>

<template>
  <div class="shell">
    <aside class="sidebar" aria-label="主导航">
      <div class="brand"><ShieldCheck :size="26" /></div>
      <button
        v-for="page in pages"
        :key="page.key"
        class="nav-button"
        :class="{ active: activeKey === page.key }"
        :title="page.label"
        @click="activeKey = page.key"
      >
        <component :is="page.icon" :size="20" />
        <span>{{ page.label }}</span>
      </button>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">Industrial AI Inspection Platform</p>
          <h1>{{ activePage.label }}</h1>
        </div>
        <div class="top-status">
          <span><Activity :size="15" /> 中心服务正常</span>
          <span><Map :size="15" /> 一号工业园</span>
          <span><BarChart3 :size="15" /> 16 路AI运行</span>
          <span>{{ loading ? '数据加载中' : appData.source === 'api' ? '中心后端联调' : '接口暂不可用' }}</span>
          <button class="ghost" @click="activeKey = 'settings'"><Settings :size="16" /> 系统配置</button>
        </div>
      </header>

      <component
        :is="activePage.component"
        v-bind="appData"
        :on-alarm-status-change="handleAlarmStatusChange"
        :on-model-publish="handleModelPublish"
        :on-task-create="handleTaskCreate"
        :on-integration-test="handleIntegrationTest"
        :on-integration-sync="handleIntegrationSync"
        :on-video-session="handleVideoSession"
        :on-snapshot="handleSnapshot"
        :on-record="handleRecord"
        :on-ai-task="handleAiTask"
        :on-flight-start="handleFlightStart"
        :on-flight-stop="handleFlightStop"
      />
    </main>
  </div>
</template>
