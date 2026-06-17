<script setup lang="ts">
import type { InspectionReport, MapEvent, MediaAsset, VehicleAsset, VehicleTrackPoint } from '../services/api'

defineProps<{
  inspectionReports: InspectionReport[]
  mediaAssets: MediaAsset[]
  mapEvents: MapEvent[]
  vehicles: VehicleAsset[]
  vehicleTracks: VehicleTrackPoint[]
}>()

function markerStyle(event: MapEvent) {
  const x = 12 + (event.longitude - 121.4725) * 260
  const y = 78 - (event.latitude - 31.2304) * 170
  return {
    left: `${Math.max(6, Math.min(92, x))}%`,
    top: `${Math.max(8, Math.min(86, y))}%`
  }
}
</script>

<template>
  <section class="page-grid">
    <div class="page-grid grid-4">
      <article class="panel kpi"><p class="panel-title">本周巡检完成率</p><div class="kpi-value tone-success">96.4%</div><div class="kpi-trend">计划 142 / 完成 137</div></article>
      <article class="panel kpi"><p class="panel-title">告警闭环时长</p><div class="kpi-value tone-primary">18m</div><div class="kpi-trend">较上周 -6m</div></article>
      <article class="panel kpi"><p class="panel-title">模型误报率</p><div class="kpi-value tone-warning">2.8%</div><div class="kpi-trend">烟火识别待复训</div></article>
      <article class="panel kpi"><p class="panel-title">无人机飞行里程</p><div class="kpi-value tone-success">42.7km</div><div class="kpi-trend">固定航线 9 次</div></article>
    </div>

    <div class="split">
      <article class="panel">
        <h2 class="panel-title">事件地图分布</h2>
        <div class="map-board">
          <div class="map-line horizontal"></div>
          <div class="map-line vertical"></div>
          <div
            v-for="event in mapEvents"
            :key="event.id"
            class="map-marker"
            :class="event.severity === 'high' ? 'danger' : 'warning'"
            :style="markerStyle(event)"
            :title="event.title"
          >
            <span></span>
          </div>
        </div>
      </article>

      <article class="panel">
        <h2 class="panel-title">事件列表</h2>
        <table class="table">
          <thead><tr><th>事件</th><th>来源</th><th>级别</th><th>坐标</th></tr></thead>
          <tbody>
            <tr v-for="event in mapEvents" :key="event.id">
              <td>{{ event.title }}</td>
              <td>{{ event.source }}</td>
              <td><span class="badge" :class="event.severity === 'high' ? 'danger' : 'warning'">{{ event.severity }}</span></td>
              <td>{{ event.latitude.toFixed(4) }}, {{ event.longitude.toFixed(4) }}</td>
            </tr>
          </tbody>
        </table>
      </article>
    </div>

    <div class="split">
      <article class="panel">
        <h2 class="panel-title">巡检报告</h2>
        <table class="table">
          <thead><tr><th>报告</th><th>周期</th><th>格式</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="report in inspectionReports" :key="report.id">
              <td>{{ report.title }}</td>
              <td>{{ report.period }}</td>
              <td>{{ report.format }}</td>
              <td><span class="badge" :class="report.status === 'generated' ? 'success' : 'warning'">{{ report.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </article>

      <article class="panel">
        <h2 class="panel-title">车载轨迹</h2>
        <div class="metric-list">
          <div v-for="vehicle in vehicles" :key="vehicle.id" class="metric-row">
            <span>{{ vehicle.plateNo }} / {{ vehicle.name }}</span>
            <strong>{{ vehicle.latitude.toFixed(4) }}, {{ vehicle.longitude.toFixed(4) }}</strong>
          </div>
          <div v-for="point in vehicleTracks" :key="`${point.vehicleId}-${point.sampledAt}`" class="metric-row">
            <span>{{ point.sampledAt }}</span>
            <strong>{{ point.speedKph.toFixed(1) }}km/h / {{ point.heading }}deg</strong>
          </div>
        </div>
      </article>
    </div>

    <article class="panel">
      <h2 class="panel-title">素材管理</h2>
        <table class="table">
          <thead><tr><th>素材</th><th>类型</th><th>来源</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="asset in mediaAssets" :key="asset.id">
              <td>{{ asset.name }}</td>
              <td>{{ asset.assetType }}</td>
              <td>{{ asset.source }}</td>
              <td><span class="badge success">{{ asset.status }}</span></td>
            </tr>
          </tbody>
        </table>
    </article>
  </section>
</template>
