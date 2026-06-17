package com.aiplatform.inspection.domain;

import java.util.List;

public record DroneAsset(
    String id,
    String dockId,
    String name,
    String vendor,
    String status,
    int batteryPercent,
    double latitude,
    double longitude,
    List<String> telemetry,
    String activeTask
) {}
