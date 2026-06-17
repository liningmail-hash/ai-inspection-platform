package com.aiplatform.inspection.adapter;

public record DroneStatus(String droneId, String status, double altitude, double speed, int batteryPercent) {}
