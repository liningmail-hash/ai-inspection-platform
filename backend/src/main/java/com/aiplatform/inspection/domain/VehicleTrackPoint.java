package com.aiplatform.inspection.domain;

public record VehicleTrackPoint(
    String vehicleId,
    double latitude,
    double longitude,
    double speedKph,
    int heading,
    String sampledAt
) {}
