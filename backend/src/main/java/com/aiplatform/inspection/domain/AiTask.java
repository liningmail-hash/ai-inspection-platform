package com.aiplatform.inspection.domain;

public record AiTask(
    String id,
    String sourceType,
    String channelId,
    String algorithmCode,
    String modelVersion,
    String status,
    double confidence,
    String evidenceUrl,
    String createdAt
) {}
