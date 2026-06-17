<script setup lang="ts">
import { PlaneTakeoff, Square } from '@lucide/vue'
import { computed, ref } from 'vue'
import {
  startFlightTask,
  stopFlightTask,
  type DroneAsset,
  type DroneSummary,
  type FlightRoute,
  type FlightTask,
  type VideoChannel
} from '../services/api'

const props = defineProps<{
  drone: DroneSummary
  drones: DroneAsset[]
  flightRoutes: FlightRoute[]
  flightTasks: FlightTask[]
  videoChannels: VideoChannel[]
  onFlightStart?: (id: string) => Promise<void>
  onFlightStop?: (id: string) => Promise<void>
}>()

const flightAction = ref({
  tone: 'muted',
  text: '等待任务操作'
})
const localTaskStatus = ref<Record<string, string>>({})

const displayedFlightTasks = computed(() =>
  props.flightTasks.map((task) => ({
    ...task,
    status: localTaskStatus.value[task.id] ?? task.status
  }))
)

function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : '接口调用失败'
}

async function runFlightAction(task: FlightTask, label: string, nextStatus: string, action: () => Promise<unknown>) {
  flightAction.value = { tone: 'warning', text: `${label}执行中：${task.routeName}` }
  try {
    await action()
    localTaskStatus.value[task.id] = nextStatus
    flightAction.value = { tone: 'success', text: `${label}成功：${task.routeName} -> ${nextStatus}` }
  } catch (error) {
    flightAction.value = { tone: 'danger', text: `${label}失败：${errorMessage(error)}` }
  }
}

function startFirstTask() {
  const task = props.flightTasks[0]
  if (!task) return
  void runFlightAction(task, '启动任务', 'running', () => startFlightTask(task.id))
}

function stopFirstTask() {
  const task = props.flightTasks[0]
  if (!task) return
  void runFlightAction(task, '终止任务', 'stopped', () => stopFlightTask(task.id))
}
</script>

<template>
  <section class="page-grid grid-main">
    <aside class="panel">
      <h2 class="panel-title">机场状态 <span class="badge success">{{ drone.status }}</span></h2>
      <div class="metric-list"><div class="metric-row"><span>电量</span><strong>{{ drone.battery }}%</strong></div><div class="progress"><span :style="{ width: `${drone.battery}%` }"></span></div><div class="metric-row"><span>天气</span><strong>{{ drone.weather }}</strong></div><div class="metric-row"><span>舱门</span><strong class="tone-success">关闭</strong></div><div class="metric-row"><span>充电</span><strong>38.4A</strong></div></div>
    </aside>
    <article class="panel">
      <h2 class="panel-title">固定航线巡检 <span class="badge success">{{ drone.route }}</span></h2>
      <div class="video">
        <div class="osd"><span>{{ videoChannels.find((item) => item.sourceType === 'drone')?.playUrl ?? 'Drone AI Video' }}</span><span class="live">LIVE</span></div>
        <div class="video-center">
          <strong>{{ drones[0]?.name ?? drone.dock }}</strong>
          <span>{{ drones[0]?.latitude.toFixed(4) }}, {{ drones[0]?.longitude.toFixed(4) }}</span>
        </div>
      </div>
      <div class="toolbar" style="margin-top: 12px;"><button class="primary-btn" @click="startFirstTask"><PlaneTakeoff :size="15" /> 启动任务</button><button class="ghost" @click="stopFirstTask"><Square :size="15" /> 终止</button></div>
      <div class="operation-result" :class="flightAction.tone">{{ flightAction.text }}</div>
      <table class="table">
        <thead><tr><th>航线</th><th>航点</th><th>高度</th><th>状态</th></tr></thead>
        <tbody>
          <tr v-for="route in flightRoutes" :key="route.id"><td>{{ route.name }}</td><td>{{ route.waypointCount }}</td><td>{{ route.altitudeMeter }}m</td><td>{{ route.status }}</td></tr>
        </tbody>
      </table>
    </article>
    <aside class="panel">
      <h2 class="panel-title">遥测数据流</h2>
      <div class="metric-list"><div v-for="item in (drones[0]?.telemetry ?? drone.telemetry)" :key="item" class="metric-row"><span>21:04:10</span><strong>{{ item }}</strong></div></div>
      <h2 class="panel-title" style="margin-top: 16px;">飞行任务</h2>
      <div class="metric-list">
        <div v-for="task in displayedFlightTasks" :key="task.id" class="metric-row">
          <span>{{ task.routeName }}</span>
          <strong>{{ task.status }}</strong>
        </div>
      </div>
    </aside>
  </section>
</template>
