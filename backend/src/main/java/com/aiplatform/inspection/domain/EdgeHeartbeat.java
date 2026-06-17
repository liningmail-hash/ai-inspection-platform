package com.aiplatform.inspection.domain;

import java.util.Map;

public record EdgeHeartbeat(
    String nodeId,
    String status,
    String ipAddress,
    Map<String, Object> gpu,
    int onlineChannels,
    int aiRunning,
    int cachedEvents
) {}
