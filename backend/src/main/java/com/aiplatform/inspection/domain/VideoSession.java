package com.aiplatform.inspection.domain;

public record VideoSession(
    String id,
    String channelId,
    String playUrl,
    String protocol,
    String status,
    String startedAt,
    String expiresAt
) {}
