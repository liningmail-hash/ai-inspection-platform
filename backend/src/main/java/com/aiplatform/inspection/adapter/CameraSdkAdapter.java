package com.aiplatform.inspection.adapter;

import com.aiplatform.inspection.domain.IntegrationConfig;
import com.aiplatform.inspection.domain.VideoChannel;

import java.util.List;

public interface CameraSdkAdapter {
    String vendor();
    AdapterResult testConnection(IntegrationConfig config);
    List<VideoChannel> syncChannels(IntegrationConfig config);
    StreamDescriptor openPreview(String channelId, String protocol);
    AdapterResult snapshot(String channelId);
    AdapterResult record(String channelId, int seconds);
}
