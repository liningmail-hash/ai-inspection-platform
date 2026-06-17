package com.aiplatform.inspection.domain;

public record MediaAsset(
    String id,
    String name,
    String assetType,
    String source,
    String relatedTask,
    String status,
    String url,
    String capturedAt
) {
}
