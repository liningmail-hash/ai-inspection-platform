package com.aiplatform.inspection.domain;

public record InspectionReport(
    String id,
    String title,
    String period,
    String status,
    String format,
    String generatedAt,
    String downloadUrl
) {
}
