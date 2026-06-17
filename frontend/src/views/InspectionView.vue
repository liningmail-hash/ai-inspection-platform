<script setup lang="ts">
import { Plus, RefreshCw, Send } from '@lucide/vue'
import type { InspectionPlan, InspectionTask } from '../services/api'

defineProps<{
  plans: InspectionPlan[]
  inspectionTasks: InspectionTask[]
  onTaskCreate?: () => Promise<void>
}>()

const statusText: Record<string, string> = {
  running: '执行中',
  scheduled: '已计划',
  queued: '排队中',
  paused: '暂停',
  completed: '已完成'
}

const typeText: Record<string, string> = {
  immediate: '即时任务',
  scheduled: '定时任务',
  loop: '循环任务'
}
</script>

<template>
  <section class="page-grid">
    <div class="toolbar">
      <input class="input" placeholder="搜索计划 / 点位 / 算法" />
      <button class="ghost"><RefreshCw :size="15" /> 刷新</button>
      <button class="primary-btn" @click="onTaskCreate?.()"><Plus :size="15" /> 创建即时任务</button>
    </div>

    <div class="split">
      <article class="panel">
        <h2 class="panel-title">任务队列</h2>
        <table class="table">
          <thead>
            <tr><th>任务</th><th>类型</th><th>优先级</th><th>执行对象</th><th>状态</th></tr>
          </thead>
          <tbody>
            <tr v-for="task in inspectionTasks" :key="task.id">
              <td>{{ task.name }}</td>
              <td>{{ typeText[task.type] ?? task.type }}</td>
              <td><span class="badge" :class="task.priority === 'high' ? 'danger' : 'warning'">{{ task.priority }}</span></td>
              <td>{{ task.route }} / {{ task.assignee }}</td>
              <td><span class="badge" :class="task.status === 'running' ? 'success' : 'warning'">{{ statusText[task.status] ?? task.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </article>

      <article class="panel">
        <h2 class="panel-title">任务下发链路 <Send :size="15" /></h2>
        <div class="metric-list">
          <div class="metric-row"><span>即时任务</span><strong>最高优先级</strong></div>
          <div class="metric-row"><span>定时任务</span><strong>日 / 周 / 月周期</strong></div>
          <div class="metric-row"><span>循环任务</span><strong>次数 + 间隔</strong></div>
          <div class="metric-row"><span>边缘执行</span><strong class="tone-success">队列已预留</strong></div>
        </div>
      </article>
    </div>

    <article class="panel">
      <h2 class="panel-title">AI巡检计划</h2>
      <table class="table">
        <thead><tr><th>计划名称</th><th>点位数</th><th>算法</th><th>执行周期</th><th>状态</th><th>规则</th></tr></thead>
        <tbody>
          <tr v-for="plan in plans" :key="plan.name">
            <td>{{ plan.name }}</td><td>{{ plan.points }}</td><td>{{ plan.algorithm }}</td><td>{{ plan.schedule }}</td>
            <td><span class="badge" :class="plan.status === '运行中' ? 'success' : 'warning'">{{ plan.status }}</span></td>
            <td>ROI / 时段 / 阈值</td>
          </tr>
        </tbody>
      </table>
    </article>

    <div class="split">
      <article class="panel">
        <h2 class="panel-title">ROI配置预览</h2>
        <div class="video"><div class="osd"><span>Polygon ROI</span><span>Threshold 0.72</span></div></div>
      </article>
      <article class="panel">
        <h2 class="panel-title">规则摘要</h2>
        <div class="metric-list">
          <div class="metric-row"><span>去重窗口</span><strong>120s</strong></div>
          <div class="metric-row"><span>告警等级</span><strong class="tone-warning">中 / 高</strong></div>
          <div class="metric-row"><span>执行时段</span><strong>00:00-24:00</strong></div>
        </div>
      </article>
    </div>
  </section>
</template>
