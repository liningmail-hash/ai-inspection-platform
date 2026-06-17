package com.aiplatform.inspection.adapter;

import java.util.Map;

public record DockStatus(String dockId, String status, int batteryPercent, Map<String, Object> weather) {}
