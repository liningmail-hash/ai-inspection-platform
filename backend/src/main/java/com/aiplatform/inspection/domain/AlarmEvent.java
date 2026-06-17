package com.aiplatform.inspection.domain;

import java.time.Instant;

public record AlarmEvent(String id, String level, String type, String device, String status, Instant detectedAt, String evidenceUrl) {}
