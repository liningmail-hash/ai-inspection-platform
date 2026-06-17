package com.aiplatform.inspection.adapter;

import com.aiplatform.inspection.domain.IntegrationConfig;
import com.aiplatform.inspection.domain.VideoChannel;

import java.util.List;
import java.util.Map;

public abstract class UnavailableCameraSdkAdapter implements CameraSdkAdapter {
    @Override
    public AdapterResult testConnection(IntegrationConfig config) {
        return unavailable("SDK connection test is not available until vendor libraries and credentials are configured");
    }

    @Override
    public List<VideoChannel> syncChannels(IntegrationConfig config) {
        return List.of();
    }

    @Override
    public StreamDescriptor openPreview(String channelId, String protocol) {
        String transport = protocol == null || protocol.isBlank() ? "webrtc" : protocol;
        return new StreamDescriptor(channelId, "", transport, 0);
    }

    @Override
    public AdapterResult snapshot(String channelId) {
        return unavailable("SDK snapshot is not available until vendor libraries and credentials are configured");
    }

    @Override
    public AdapterResult record(String channelId, int seconds) {
        return unavailable("SDK recording is not available until vendor libraries and credentials are configured");
    }

    private AdapterResult unavailable(String message) {
        return new AdapterResult(false, vendor() + " " + message, Map.of(
            "vendor", vendor(),
            "sdkRequired", true
        ));
    }
}
