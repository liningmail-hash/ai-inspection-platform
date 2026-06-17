# ADR 0002：前端采用 Vue 3 + TypeScript + Vite

## 状态

Accepted

## 背景

平台前端是典型工业后台工作台，需要大量表格、状态面板、视频窗口、配置表单和实时事件流。

## 决策

一期前端采用 Vue 3 + TypeScript + Vite。

## 理由

- Vue 3 适合中后台系统快速构建。
- TypeScript 能提升接口联调和组件维护质量。
- Vite 启动和构建速度快，适合快速迭代。
- 生态成熟，后续可接入 Element Plus、Naive UI 或自研组件体系。

## 影响

- 所有 API 数据结构需要类型化。
- 页面组件不能长期堆业务逻辑，后续要拆分 `components/` 和 `services/`。
- 前端每次提交至少运行 `npm run build`。
