package com.aiplatform.inspection.domain;

public record InspectionPlan(String id, String name, int pointCount, String algorithm, String schedule, String status) {}
