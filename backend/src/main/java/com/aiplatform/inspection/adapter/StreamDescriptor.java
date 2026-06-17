package com.aiplatform.inspection.adapter;

public record StreamDescriptor(String channelId, String url, String transport, int latencyMs) {}
