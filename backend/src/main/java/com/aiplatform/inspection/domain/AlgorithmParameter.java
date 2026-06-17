package com.aiplatform.inspection.domain;

public record AlgorithmParameter(
    String id,
    String algorithmCode,
    String algorithmName,
    double threshold,
    int sensitivity,
    boolean enabled,
    String updatedAt
) {
}
