<script setup lang="ts">
import type { AuditLog, SystemRole, SystemUser } from '../services/api'

defineProps<{
  systemUsers: SystemUser[]
  systemRoles: SystemRole[]
  auditLogs: AuditLog[]
  source: 'api' | 'mock'
}>()

function formatTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(date)
}
</script>

<template>
  <section class="page-grid">
    <div class="split">
      <article class="panel">
        <h2 class="panel-title">登录与权限</h2>
        <div class="metric-list">
          <div class="metric-row"><span>认证模式</span><strong>试点演示令牌</strong></div>
          <div class="metric-row"><span>演示账号</span><strong>admin / demo123</strong></div>
          <div class="metric-row"><span>数据来源</span><strong>{{ source === 'api' ? '中心后端 API' : '接口暂不可用' }}</strong></div>
          <div class="metric-row"><span>生产要求</span><strong>替换为 OAuth2/JWT + 密码加密</strong></div>
        </div>
      </article>

      <article class="panel">
        <h2 class="panel-title">协议与集成</h2>
        <div class="metric-list">
          <div class="metric-row"><span>设备协议</span><strong>海康 / 大华 / ONVIF / GB28181 / JT1078</strong></div>
          <div class="metric-row"><span>无人机适配</span><strong>厂商适配层</strong></div>
          <div class="metric-row"><span>告警推送</span><strong>短信 / 企业微信 / Webhook 预留</strong></div>
          <div class="metric-row"><span>视频播放</span><strong>WebRTC 优先 / HLS 兼容</strong></div>
        </div>
      </article>
    </div>

    <article class="panel">
      <h2 class="panel-title">用户管理</h2>
      <table class="table">
        <thead>
          <tr><th>账号</th><th>姓名</th><th>组织/站点</th><th>角色</th><th>状态</th></tr>
        </thead>
        <tbody>
          <tr v-for="user in systemUsers" :key="user.id">
            <td>{{ user.username }}</td>
            <td>{{ user.displayName }}</td>
            <td>{{ user.organization }} / {{ user.site }}</td>
            <td>{{ user.roles.join('、') }}</td>
            <td><span class="badge success">{{ user.status === 'active' ? '启用' : user.status }}</span></td>
          </tr>
        </tbody>
      </table>
    </article>

    <div class="split">
      <article class="panel">
        <h2 class="panel-title">角色管理</h2>
        <table class="table">
          <thead>
            <tr><th>角色</th><th>权限</th><th>用户数</th></tr>
          </thead>
          <tbody>
            <tr v-for="role in systemRoles" :key="role.id">
              <td>
                <strong>{{ role.name }}</strong>
                <div class="muted">{{ role.code }}</div>
              </td>
              <td>{{ role.permissions.join('、') }}</td>
              <td>{{ role.userCount }}</td>
            </tr>
          </tbody>
        </table>
      </article>

      <article class="panel">
        <h2 class="panel-title">审计日志</h2>
        <table class="table">
          <thead>
            <tr><th>时间</th><th>操作人</th><th>动作</th><th>对象</th><th>结果</th></tr>
          </thead>
          <tbody>
            <tr v-for="log in auditLogs" :key="log.id">
              <td>{{ formatTime(log.createdAt) }}</td>
              <td>{{ log.actor }}</td>
              <td>{{ log.action }}</td>
              <td>{{ log.targetType }} / {{ log.targetId }}</td>
              <td><span class="badge success">{{ log.result }}</span></td>
            </tr>
          </tbody>
        </table>
      </article>
    </div>

    <article class="panel">
      <h2 class="panel-title">系统运行参数</h2>
      <table class="table">
        <thead><tr><th>参数</th><th>当前值</th><th>建议</th><th>状态</th></tr></thead>
        <tbody>
          <tr><td>边缘断网缓存</td><td>24h</td><td>试点可用，生产建议 72h</td><td><span class="badge success">正常</span></td></tr>
          <tr><td>训练算力</td><td>单机 GPU</td><td>二期扩展 GPU 队列</td><td><span class="badge warning">观察</span></td></tr>
          <tr><td>证据留存</td><td>30 天</td><td>按项目合规调整</td><td><span class="badge success">正常</span></td></tr>
          <tr><td>模型发布审批</td><td>开启</td><td>生产必须保留</td><td><span class="badge success">正常</span></td></tr>
        </tbody>
      </table>
    </article>
  </section>
</template>
