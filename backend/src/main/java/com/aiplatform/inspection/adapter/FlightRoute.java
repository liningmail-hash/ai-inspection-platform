package com.aiplatform.inspection.adapter;

import java.util.List;

public record FlightRoute(String name, List<Waypoint> waypoints, int altitudeMeter) {}
