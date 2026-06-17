<script setup lang="ts">
import type { AiTask, Alarm, Device, DroneSummary, InspectionPlan, Kpi, VehicleAsset, VideoChannel } from '../services/api'

defineProps<{
  kpis: Kpi[]
  alarms: Alarm[]
  devices: Device[]
  drone: DroneSummary
  plans: InspectionPlan[]
  videoChannels: VideoChannel[]
  vehicles: VehicleAsset[]
  aiTasks: AiTask[]
}>()
</script>

<template>
  <section class="page-grid">
    <div class="page-grid grid-4">
      <article v-for="item in kpis" :key="item.label" class="panel kpi">
        <p class="panel-title">{{ item.label }}</p>
        <div class="kpi-value" :class="`tone-${item.tone}`">{{ item.value }}</div>
        <div class="kpi-trend">{{ item.trend }}</div>
      </article>
    </div>

    <div class="page-grid grid-main">
      <article class="panel">
        <h2 class="panel-title">设备健康</h2>
        <div class="metric-list">
          <div v-for="device in devices" :key="device.name" class="metric-row">
            <span>{{ device.name }}</span>
            <strong :class="device.status === 'online' ? 'tone-success' : 'tone-warning'">{{ device.status }}</strong>
          </div>
        </div>
      </article>

      <article class="panel">
        <h2 class="panel-title">实时巡检视图 <span class="badge success">三线接入</span></h2>
        <div class="video">
          <div class="osd"><span>AI {{ aiTasks.length }}CH / {{ videoChannels.length }} 路视频</span><span class="live">LIVE</span></div>
          <div class="video-center">
            <strong>厂区 + 无人机 + 车载</strong>
            <span>{{ videoChannels.map((item) => item.sourceType).filter((item, index, array) => array.indexOf(item) === index).join(' / ') }}</span>
          </div>
        </div>
      </article>

      <article class="panel">
        <h2 class="panel-title">无人机任务</h2>
        <div class="metric-list">
          <div class="metric-row"><span>机场</span><strong>{{ drone.dock }}</strong></div>
          <div class="metric-row"><span>状态</span><strong class="tone-success">{{ drone.status }}</strong></div>
          <div class="metric-row"><span>电量</span><strong>{{ drone.battery }}%</strong></div>
          <div class="progress"><span :style="{ width: `${drone.battery}%` }"></span></div>
          <div class="metric-row"><span>航线</span><strong>{{ drone.route }}</strong></div>
          <div class="metric-row"><span>车载在线</span><strong>{{ vehicles.filter((item) => item.status === 'online').length }} 台</strong></div>
        </div>
      </article>
    </div>

    <div class="split">
      <article class="panel">
        <h2 class="panel-title">最近告警</h2>
        <table class="table">
          <thead><tr><th>等级</th><th>类型</th><th>点位</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="alarm in alarms" :key="alarm.id">
              <td><span class="badge" :class="alarm.level === '高' ? 'danger' : alarm.level === '中' ? 'warning' : 'success'">{{ alarm.level }}</span></td>
              <td>{{ alarm.type }}</td>
              <td>{{ alarm.device }}</td>
              <td>{{ alarm.status }}</td>
            </tr>
          </tbody>
        </table>
      </article>

      <article class="panel">
        <h2 class="panel-title">巡检计划</h2>
        <table class="table">
          <thead><tr><th>计划</th><th>点位</th><th>算法</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="plan in plans" :key="plan.name">
              <td>{{ plan.name }}</td>
              <td>{{ plan.points }}</td>
              <td>{{ plan.algorithm }}</td>
              <td><span class="badge" :class="plan.status === '运行中' ? 'success' : 'warning'">{{ plan.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </article>
    </div>
  </section>
</template>
