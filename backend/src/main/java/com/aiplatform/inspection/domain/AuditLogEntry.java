package com.aiplatform.inspection.domain;

import java.time.Instant;

public record AuditLogEntry(
    String id,
    String actor,
    String action,
    String targetType,
    String targetId,
    String result,
    Instant createdAt
) {
}
