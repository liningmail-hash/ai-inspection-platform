package com.aiplatform.inspection.domain;

import java.time.Instant;
import java.util.Map;

public record CommandResult(String id, String status, String message, Instant updatedAt, Map<String, Object> data) {}
