package com.aiplatform.inspection.domain;

public record IntegrationConfig(
    String id,
    String name,
    String sourceType,
    String vendor,
    String sdkType,
    String status,
    String endpoint,
    String credentialRef,
    String lastSyncAt,
    int channelCount
) {}
