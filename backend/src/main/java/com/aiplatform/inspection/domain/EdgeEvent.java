package com.aiplatform.inspection.domain;

import java.time.Instant;

public record EdgeEvent(
    String id,
    String eventType,
    String sourceType,
    String sourceId,
    String severity,
    String title,
    String status,
    double latitude,
    double longitude,
    String evidenceUrl,
    Instant detectedAt
) {}
