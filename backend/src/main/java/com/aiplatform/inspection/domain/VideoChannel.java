package com.aiplatform.inspection.domain;

public record VideoChannel(
    String id,
    String sourceId,
    String sourceType,
    String sourceName,
    String name,
    String protocol,
    String streamUrl,
    String playUrl,
    boolean aiEnabled,
    String status,
    String edgeNode,
    double latitude,
    double longitude
) {}
