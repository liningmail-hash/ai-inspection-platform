<script setup lang="ts">
import { CheckCircle2, ExternalLink, XCircle } from '@lucide/vue'
import { computed, ref } from 'vue'
import { updateAlarmStatus, type Alarm } from '../services/api'

const props = defineProps<{
  alarms: Alarm[]
  onAlarmStatusChange?: (id: string, status: string) => Promise<void>
}>()

const alarmAction = ref({
  tone: 'muted',
  text: '等待处置'
})
const localStatus = ref<Record<string, string>>({})

const statusText: Record<string, string> = {
  processing: '处理中',
  false_positive: '误报',
  dispatched: '已派单'
}

const displayedAlarms = computed(() =>
  props.alarms.map((alarm) => ({
    ...alarm,
    status: localStatus.value[alarm.id] ?? alarm.status
  }))
)

function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : '接口调用失败'
}

async function updateFirstAlarm(status: string) {
  const alarm = props.alarms[0]
  if (!alarm) return
  const nextStatus = statusText[status] ?? status
  alarmAction.value = { tone: 'warning', text: `告警${alarm.id}处置中：${nextStatus}` }
  try {
    await updateAlarmStatus(alarm.id, status)
    localStatus.value[alarm.id] = nextStatus
    alarmAction.value = { tone: 'success', text: `告警${alarm.id}已更新为${nextStatus}` }
  } catch (error) {
    alarmAction.value = { tone: 'danger', text: `告警${alarm.id}更新失败：${errorMessage(error)}` }
  }
}
</script>

<template>
  <section class="page-grid grid-main">
    <article class="panel" style="grid-column: span 2;">
      <div class="toolbar"><input class="input" placeholder="告警类型 / 设备 / 编号" /><button class="ghost">等级</button><button class="ghost">状态</button><button class="ghost">时间</button></div>
      <table class="table">
        <thead><tr><th>编号</th><th>等级</th><th>类型</th><th>点位</th><th>时间</th><th>状态</th></tr></thead>
        <tbody>
          <tr v-for="alarm in displayedAlarms" :key="alarm.id">
            <td>{{ alarm.id }}</td><td><span class="badge" :class="alarm.level === '高' ? 'danger' : alarm.level === '中' ? 'warning' : 'success'">{{ alarm.level }}</span></td><td>{{ alarm.type }}</td><td>{{ alarm.device }}</td><td>{{ alarm.time }}</td><td>{{ alarm.status }}</td>
          </tr>
        </tbody>
      </table>
    </article>
    <aside class="panel">
      <h2 class="panel-title">告警详情</h2>
      <div class="video"><div class="osd"><span>Evidence Clip</span><span class="live">ALARM</span></div></div>
      <div class="toolbar" style="margin-top: 12px;"><button class="primary-btn" @click="updateFirstAlarm('processing')"><CheckCircle2 :size="15" /> 确认</button><button class="ghost" @click="updateFirstAlarm('false_positive')"><XCircle :size="15" /> 误报</button><button class="ghost" @click="updateFirstAlarm('dispatched')"><ExternalLink :size="15" /> 派单</button></div>
      <div class="operation-result" :class="alarmAction.tone">{{ alarmAction.text }}</div>
      <div class="metric-list"><div class="metric-row"><span>处置状态</span><strong class="tone-warning">{{ displayedAlarms[0]?.status ?? '待确认' }}</strong></div><div class="metric-row"><span>证据</span><strong>{{ alarms[0]?.evidenceUrl ?? '图片 + 12s视频' }}</strong></div><div class="metric-row"><span>来源</span><strong>{{ alarms[0]?.device ?? '-' }}</strong></div><div class="metric-row"><span>回调</span><strong>Webhook Ready</strong></div></div>
    </aside>
  </section>
</template>
