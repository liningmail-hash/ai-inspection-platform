package com.aiplatform.inspection.domain;

public record VehicleAsset(
    String id,
    String plateNo,
    String name,
    String vendor,
    String status,
    double speedKph,
    double latitude,
    double longitude,
    String lastSeenAt
) {}
