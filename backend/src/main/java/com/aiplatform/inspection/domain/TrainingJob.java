package com.aiplatform.inspection.domain;

import java.util.Map;

public record TrainingJob(String id, String name, String status, int progress, Map<String, Object> metrics) {}
