package com.aiplatform.inspection.adapter;

import com.aiplatform.inspection.domain.IntegrationConfig;
import com.aiplatform.inspection.domain.VehicleAsset;
import com.aiplatform.inspection.domain.VehicleTrackPoint;
import com.aiplatform.inspection.domain.VideoChannel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class MockVendorSdkAdapter implements CameraSdkAdapter, VehicleVideoAdapter {
    @Override
    public String vendor() {
        return "MOCK_VENDOR";
    }

    @Override
    public AdapterResult testConnection(IntegrationConfig config) {
        return new AdapterResult(true, config.vendor() + " SDK gateway reachable", Map.of(
            "sourceType", config.sourceType(),
            "endpoint", config.endpoint(),
            "checkedAt", Instant.now().toString()
        ));
    }

    @Override
    public List<VideoChannel> syncChannels(IntegrationConfig config) {
        if ("vehicle".equals(config.sourceType())) {
            return syncChannels("vehicle-001");
        }
        return List.of(
            new VideoChannel("sync-cam-001", "dev-001", "camera", "Area A camera 01", "Main stream", "HIK_SDK", "rtsp://sdk/hik/a1", "http://localhost:8088/live/camera-a1.flv", true, "online", "EDGE-01", 31.2312, 121.4741),
            new VideoChannel("sync-drone-001", "drone-001", "drone", "Dock drone 01", "Drone live stream", "DJI_SDK", "rtsp://sdk/dji/drone-001", "http://localhost:8088/live/drone-001.flv", true, "online", "EDGE-01", 31.2321, 121.4752)
        );
    }

    @Override
    public StreamDescriptor openPreview(String channelId, String protocol) {
        String transport = protocol == null || protocol.isBlank() ? "webrtc" : protocol;
        return new StreamDescriptor(channelId, "http://localhost:8088/live/" + channelId + "." + ("hls".equals(transport) ? "m3u8" : "flv"), transport, 92);
    }

    @Override
    public AdapterResult snapshot(String channelId) {
        return new AdapterResult(true, "snapshot captured", Map.of(
            "channelId", channelId,
            "url", "minio://evidence/" + channelId + "-snapshot.jpg"
        ));
    }

    @Override
    public AdapterResult record(String channelId, int seconds) {
        return new AdapterResult(true, "recording requested", Map.of(
            "channelId", channelId,
            "seconds", Math.max(5, seconds),
            "url", "minio://recordings/" + channelId + "-" + Instant.now().toEpochMilli() + ".mp4"
        ));
    }

    @Override
    public List<VehicleAsset> syncVehicles(IntegrationConfig config) {
        return List.of(
            new VehicleAsset("vehicle-001", "HU-A-D8123", "Hazmat transport vehicle 01", "JT1078_GATEWAY", "online", 36.5, 31.2298, 121.4718, Instant.now().toString()),
            new VehicleAsset("vehicle-002", "HU-A-F2639", "Patrol vehicle 02", "JT1078_GATEWAY", "online", 18.2, 31.2330, 121.4760, Instant.now().toString())
        );
    }

    @Override
    public List<VideoChannel> syncChannels(String vehicleId) {
        return List.of(
            new VideoChannel("veh-ch-001", vehicleId, "vehicle", "Hazmat transport vehicle 01", "Front camera", "JT1078", "jt1078://vehicle-001/front", "http://localhost:8088/live/vehicle-001-front.flv", true, "online", "EDGE-01", 31.2298, 121.4718),
            new VideoChannel("veh-ch-002", vehicleId, "vehicle", "Hazmat transport vehicle 01", "Cargo camera", "JT1078", "jt1078://vehicle-001/cargo", "http://localhost:8088/live/vehicle-001-cargo.flv", true, "online", "EDGE-01", 31.2298, 121.4718)
        );
    }

    @Override
    public List<VehicleTrackPoint> queryTrack(String vehicleId) {
        return List.of(
            new VehicleTrackPoint(vehicleId, 31.2298, 121.4718, 36.5, 86, "2026-06-16T09:20:00+08:00"),
            new VehicleTrackPoint(vehicleId, 31.2304, 121.4726, 32.0, 92, "2026-06-16T09:24:00+08:00"),
            new VehicleTrackPoint(vehicleId, 31.2311, 121.4734, 28.2, 104, "2026-06-16T09:28:00+08:00")
        );
    }
}
