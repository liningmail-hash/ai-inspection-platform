package com.aiplatform.inspection.domain;

import java.time.Instant;

public record MapEvent(
    String id,
    String title,
    String type,
    String severity,
    String status,
    double latitude,
    double longitude,
    String source,
    Instant detectedAt
) {
}
