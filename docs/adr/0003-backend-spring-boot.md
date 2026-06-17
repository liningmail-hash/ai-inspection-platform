# ADR 0003：中心后端采用 Java 21 + Spring Boot 3

## 状态

Accepted

## 背景

中心后端承担设备、巡检、告警、权限、模型发布、无人机任务等核心业务。系统后续需要私有化交付、长期维护和企业集成。

## 决策

中心后端采用 Java 21 + Spring Boot 3。

## 理由

- Spring Boot 适合企业级业务系统和私有化交付。
- Java 生态在权限、审计、数据库、消息队列、协议集成方面成熟。
- Java 21 提供长期演进空间。
- OpenAPI、Actuator、WebSocket 等能力集成成本低。

## 影响

- 后端分层必须清晰：Controller、Service、Domain、Adapter、Repository。
- 厂商 SDK 不得绕过 Adapter 直接进入业务服务。
- 后续数据库迁移建议使用 Flyway。
