<script setup lang="ts">
import { Bot, Camera, Maximize2, Play, Radio, RotateCcw, Scissors } from '@lucide/vue'
import { createAiTask, createVideoSession, recordChannel, snapshotChannel, type AiTask, type Device, type VideoChannel } from '../services/api'
import { ref } from 'vue'

const props = defineProps<{
  devices: Device[]
  videoChannels: VideoChannel[]
  aiTasks: AiTask[]
  onVideoSession?: (channelId: string) => Promise<void>
  onSnapshot?: (channelId: string) => Promise<void>
  onRecord?: (channelId: string) => Promise<void>
  onAiTask?: (channelId: string) => Promise<void>
}>()

const sourceText: Record<string, string> = {
  camera: '厂区',
  drone: '无人机',
  vehicle: '车载'
}

const operationStatus = ref({
  tone: 'muted',
  text: '等待操作'
})

function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : '接口调用失败'
}

async function runVideoAction(label: string, channel: VideoChannel, action: () => Promise<unknown>) {
  operationStatus.value = { tone: 'warning', text: `${label}执行中：${channel.sourceName} / ${channel.name}` }
  try {
    await action()
    operationStatus.value = { tone: 'success', text: `${label}成功：${channel.sourceName} / ${channel.name}` }
  } catch (error) {
    operationStatus.value = { tone: 'danger', text: `${label}失败：${errorMessage(error)}` }
  }
}

function openStream(channel: VideoChannel) {
  void runVideoAction('开流', channel, () => createVideoSession(channel.id))
}

function createChannelAiTask(channel: VideoChannel) {
  const currentTask = props.aiTasks.find((item) => item.channelId === channel.id)
  void runVideoAction('AI任务', channel, () =>
    createAiTask({
      channelId: channel.id,
      algorithmCode: currentTask?.algorithmCode ?? 'smoke_fire',
      modelVersion: currentTask?.modelVersion ?? 'production'
    })
  )
}

function snapshotFirstChannel() {
  const channel = props.videoChannels[0]
  if (!channel) return
  void runVideoAction('抓图', channel, () => snapshotChannel(channel.id))
}

function recordFirstChannel() {
  const channel = props.videoChannels[0]
  if (!channel) return
  void runVideoAction('录像', channel, () => recordChannel(channel.id))
}
</script>

<template>
  <section class="page-grid grid-main">
    <aside class="panel">
      <h2 class="panel-title">接入树 <span class="badge success">{{ videoChannels.filter((item) => item.status === 'online').length }} 在线</span></h2>
      <div class="metric-list">
        <div v-for="channel in videoChannels" :key="channel.id" class="metric-row">
          <span>{{ channel.sourceName }}</span>
          <strong>{{ sourceText[channel.sourceType] }} / {{ channel.protocol }}</strong>
        </div>
      </div>
    </aside>

    <article class="panel">
      <div class="panel-title">
        <span>视频宫格</span>
        <span class="toolbar"><button class="ghost">1</button><button class="ghost">4</button><button class="ghost">9</button><button class="ghost">16</button></span>
      </div>
      <div class="channel-grid">
        <div v-for="channel in videoChannels" :key="channel.id" class="video channel-tile">
          <div class="osd"><span>{{ sourceText[channel.sourceType] }} / {{ channel.name }}</span><span class="live">LIVE</span></div>
          <div class="video-center">
            <strong>{{ channel.sourceName }}</strong>
            <span>{{ channel.playUrl }}</span>
          </div>
          <div class="channel-actions">
            <button class="ghost" @click="openStream(channel)"><Play :size="14" /> 开流</button>
            <button class="ghost" @click="createChannelAiTask(channel)"><Bot :size="14" /> AI</button>
          </div>
        </div>
      </div>
    </article>

    <aside class="panel">
      <h2 class="panel-title">通道控制</h2>
      <div class="toolbar"><button class="primary-btn" @click="snapshotFirstChannel"><Camera :size="15" /> 抓图</button><button class="ghost" @click="recordFirstChannel"><Scissors :size="15" /> 录像</button></div>
      <div class="toolbar"><button class="ghost"><RotateCcw :size="15" /> PTZ</button><button class="ghost"><Maximize2 :size="15" /> 全屏</button></div>
      <div class="operation-result" :class="operationStatus.tone">{{ operationStatus.text }}</div>
      <div class="metric-list">
        <div class="metric-row"><span>AI状态</span><strong class="tone-success">{{ aiTasks.filter((item) => item.status === 'running').length }} 路运行</strong></div>
        <div class="metric-row"><span>算法</span><strong>{{ aiTasks.map((item) => item.algorithmCode).slice(0, 2).join(' / ') }}</strong></div>
        <div class="metric-row"><span>推理节点</span><strong>{{ videoChannels[0]?.edgeNode ?? 'EDGE-01' }}</strong></div>
        <div class="metric-row"><span>上报链路</span><strong><Radio :size="13" /> MQTT</strong></div>
        <div class="metric-row"><span>设备兜底</span><strong>{{ devices.length }} 台档案</strong></div>
      </div>
    </aside>
  </section>
</template>
