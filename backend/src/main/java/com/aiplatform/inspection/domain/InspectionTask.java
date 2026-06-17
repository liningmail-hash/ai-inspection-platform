package com.aiplatform.inspection.domain;

public record InspectionTask(
    String id,
    String name,
    String type,
    String priority,
    String status,
    String route,
    String assignee,
    String plannedAt
) {
}
