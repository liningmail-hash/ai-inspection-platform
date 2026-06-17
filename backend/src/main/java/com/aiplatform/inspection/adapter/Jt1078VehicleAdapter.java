package com.aiplatform.inspection.adapter;

import com.aiplatform.inspection.domain.IntegrationConfig;
import com.aiplatform.inspection.domain.VehicleAsset;
import com.aiplatform.inspection.domain.VehicleTrackPoint;
import com.aiplatform.inspection.domain.VideoChannel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Jt1078VehicleAdapter implements VehicleVideoAdapter {
    @Override
    public String vendor() {
        return "JT1078";
    }

    @Override
    public AdapterResult testConnection(IntegrationConfig config) {
        return new AdapterResult(false, "JT1078 gateway SDK is not available until gateway credentials are configured", Map.of(
            "vendor", vendor(),
            "sdkRequired", true
        ));
    }

    @Override
    public List<VehicleAsset> syncVehicles(IntegrationConfig config) {
        return List.of();
    }

    @Override
    public List<VideoChannel> syncChannels(String vehicleId) {
        return List.of();
    }

    @Override
    public List<VehicleTrackPoint> queryTrack(String vehicleId) {
        return List.of();
    }

    @Override
    public StreamDescriptor openPreview(String channelId, String protocol) {
        String transport = protocol == null || protocol.isBlank() ? "webrtc" : protocol;
        return new StreamDescriptor(channelId, "", transport, 0);
    }
}
