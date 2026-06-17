package com.aiplatform.inspection.domain;

public record FlightRouteSummary(
    String id,
    String dockId,
    String name,
    int waypointCount,
    int altitudeMeter,
    String status
) {}
