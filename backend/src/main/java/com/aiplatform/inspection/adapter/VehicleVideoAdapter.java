package com.aiplatform.inspection.adapter;

import com.aiplatform.inspection.domain.IntegrationConfig;
import com.aiplatform.inspection.domain.VehicleAsset;
import com.aiplatform.inspection.domain.VehicleTrackPoint;
import com.aiplatform.inspection.domain.VideoChannel;

import java.util.List;
import java.util.Map;

public interface VehicleVideoAdapter {
    String vendor();
    AdapterResult testConnection(IntegrationConfig config);
    List<VehicleAsset> syncVehicles(IntegrationConfig config);
    List<VideoChannel> syncChannels(String vehicleId);
    List<VehicleTrackPoint> queryTrack(String vehicleId);
    StreamDescriptor openPreview(String channelId, String protocol);

    default AdapterResult snapshot(String channelId) {
        return new AdapterResult(false, "vehicle channel snapshot is unsupported by " + vendor() + " adapter", Map.of(
            "vendor", vendor(),
            "channelId", channelId,
            "operation", "snapshot",
            "unsupported", true
        ));
    }

    default AdapterResult record(String channelId, int seconds) {
        return new AdapterResult(false, "vehicle channel recording is unsupported by " + vendor() + " adapter", Map.of(
            "vendor", vendor(),
            "channelId", channelId,
            "operation", "record",
            "seconds", seconds,
            "unsupported", true
        ));
    }
}
