package com.aiplatform.inspection.domain;

import java.util.Map;

public record ModelVersion(String id, String algorithm, String version, String status, Map<String, Object> metrics) {}
