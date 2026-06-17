<!--
  CHANGELOG.md — 版本变更记录
  格式遵循 Keep a Changelog (https://keepachangelog.com)
  分类：Added / Changed / Deprecated / Removed / Fixed / Security
-->

# Changelog

All notable changes to this project will be documented in this file.

## [v0.1.0] — 2026-06-17

### Added
- 初始项目架构：frontend + backend + ai-service + edge-simulator + database + deploy + docs
- Vue 3 + Vite + TypeScript 前端（11 个页面视图）
- Spring Boot 3 后端（设备管理、巡检任务、告警、AI 任务、视频会话）
- 设备适配器框架（Dahua / Hikvision / DJI / JT1078 / Mock）
- FastAPI AI 推理服务 + 边缘模拟器
- PostgreSQL 建库脚本与数据迁移
- Docker Compose 部署配置（本地 + 云端）
- 完整文档体系（36 个文件，含架构/API/运维/测试/ADR）
- 云端 Mock 联调环境已部署（114.67.114.201）
