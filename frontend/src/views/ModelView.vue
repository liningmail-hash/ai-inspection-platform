<script setup lang="ts">
import { RotateCcw, Rocket } from '@lucide/vue'
import type { AlgorithmParameter, Model } from '../services/api'

defineProps<{
  models: Model[]
  algorithmParameters: AlgorithmParameter[]
  onModelPublish?: (id: string, status: 'canary' | 'production' | 'archived') => Promise<void>
}>()
</script>

<template>
  <section class="page-grid">
    <article class="panel">
      <h2 class="panel-title">模型生命周期</h2>
      <table class="table">
        <thead><tr><th>算法</th><th>版本</th><th>准确率</th><th>召回率</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="model in models" :key="model.algorithm">
            <td>{{ model.algorithm }}</td>
            <td>{{ model.version }}</td>
            <td>{{ model.precision }}</td>
            <td>{{ model.recall }}</td>
            <td><span class="badge" :class="model.status === '生产' ? 'success' : model.status === '灰度' ? 'warning' : ''">{{ model.status }}</span></td>
            <td>
              <button class="ghost" @click="model.id && onModelPublish?.(model.id, 'canary')"><Rocket :size="14" /> 灰度</button>
              <button class="ghost" @click="model.id && onModelPublish?.(model.id, 'archived')"><RotateCcw :size="14" /> 回滚</button>
            </td>
          </tr>
        </tbody>
      </table>
    </article>

    <article class="panel">
      <h2 class="panel-title">算法参数</h2>
      <table class="table">
        <thead><tr><th>算法</th><th>编码</th><th>阈值</th><th>灵敏度</th><th>状态</th></tr></thead>
        <tbody>
          <tr v-for="item in algorithmParameters" :key="item.id">
            <td>{{ item.algorithmName }}</td>
            <td>{{ item.algorithmCode }}</td>
            <td>
              <div class="progress parameter"><span :style="{ width: `${item.threshold * 100}%` }"></span></div>
              <div class="muted">{{ item.threshold.toFixed(2) }}</div>
            </td>
            <td>{{ item.sensitivity }}</td>
            <td><span class="badge" :class="item.enabled ? 'success' : 'warning'">{{ item.enabled ? '启用' : '停用' }}</span></td>
          </tr>
        </tbody>
      </table>
    </article>

    <div class="split">
      <article class="panel">
        <h2 class="panel-title">评估报告</h2>
        <div class="metric-list">
          <div class="metric-row"><span>误报率</span><strong>2.8%</strong></div>
          <div class="metric-row"><span>漏报率</span><strong>4.1%</strong></div>
          <div class="metric-row"><span>边缘兼容</span><strong class="tone-success">ONNX/TensorRT</strong></div>
        </div>
      </article>
      <article class="panel">
        <h2 class="panel-title">发布策略</h2>
        <div class="metric-list">
          <div class="metric-row"><span>灰度节点</span><strong>EDGE-01</strong></div>
          <div class="metric-row"><span>回滚窗口</span><strong>24h</strong></div>
          <div class="metric-row"><span>审批</span><strong>待审核</strong></div>
        </div>
      </article>
    </div>
  </section>
</template>
