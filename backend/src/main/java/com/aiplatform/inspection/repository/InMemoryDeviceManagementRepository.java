package com.aiplatform.inspection.repository;

import com.aiplatform.inspection.domain.Device;
import com.aiplatform.inspection.domain.VideoChannel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("!postgres")
public class InMemoryDeviceManagementRepository implements DeviceManagementRepository {
    private final Map<String, Device> devices = new ConcurrentHashMap<>();
    private final Map<String, VideoChannel> channels = new ConcurrentHashMap<>();

    public InMemoryDeviceManagementRepository() {
        seedDevices().forEach(device -> devices.put(device.id(), device));
        seedChannels().forEach(channel -> channels.put(channel.id(), channel));
    }

    @Override
    public List<Device> devices() {
        return devices.values().stream()
            .filter(device -> !"deleted".equals(device.status()))
            .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
            .toList();
    }

    @Override
    public Optional<Device> deviceById(String id) {
        return Optional.ofNullable(devices.get(id))
            .filter(device -> !"deleted".equals(device.status()));
    }

    @Override
    public Device createDevice(Device device) {
        devices.put(device.id(), device);
        return device;
    }

    @Override
    public Optional<Device> updateDevice(String id, Device device) {
        if (!devices.containsKey(id) || "deleted".equals(devices.get(id).status())) {
            return Optional.empty();
        }
        devices.put(id, device);
        return Optional.of(device);
    }

    @Override
    public Optional<Device> updateDeviceStatus(String id, String status, Instant updatedAt) {
        Device current = devices.get(id);
        if (current == null || "deleted".equals(current.status())) {
            return Optional.empty();
        }
        Device updated = new Device(current.id(), current.name(), current.sourceType(), current.vendor(), current.protocol(), current.endpoint(), current.credentialRef(), current.location(), current.edgeNodeId(), status, current.streamUrl(), current.createdAt(), updatedAt.toString());
        devices.put(id, updated);
        return Optional.of(updated);
    }

    @Override
    public boolean deleteDevice(String id, Instant deletedAt) {
        Device current = devices.get(id);
        if (current == null || "deleted".equals(current.status())) {
            return false;
        }
        devices.put(id, new Device(current.id(), current.name(), current.sourceType(), current.vendor(), current.protocol(), current.endpoint(), current.credentialRef(), current.location(), current.edgeNodeId(), "deleted", current.streamUrl(), current.createdAt(), deletedAt.toString()));
        return true;
    }

    @Override
    public List<VideoChannel> replaceDeviceChannels(String deviceId, List<VideoChannel> newChannels, Instant syncedAt) {
        channels.entrySet().removeIf(entry -> entry.getValue().sourceId().equals(deviceId));
        newChannels.forEach(channel -> channels.put(channel.id(), channel));
        return newChannels;
    }

    private List<Device> seedDevices() {
        return List.of(
            new Device("dev-001", "Area A camera 01", "camera", "HIKVISION", "GB28181", "rtsp://example/live/a1", "secret://integrations/hikvision", "Area A warehouse", "EDGE-01", "online", "rtsp://example/live/a1", "2026-06-16T09:00:00Z", "2026-06-16T09:00:00Z"),
            new Device("drone-001", "Dock drone 01", "drone", "DJI_DOCK", "DJI_CLOUD_API", "https://dji-dock.local", "secret://integrations/dji", "East dock", "EDGE-01", "online", "rtsp://example/live/drone-001", "2026-06-16T09:00:00Z", "2026-06-16T09:00:00Z"),
            new Device("vehicle-001", "Hazmat vehicle 01", "vehicle", "JT1078", "JT/T1078", "https://jt1078-gateway.local", "secret://integrations/jt1078", "Factory road", "EDGE-01", "online", "jt1078://vehicle-001/front", "2026-06-16T09:00:00Z", "2026-06-16T09:00:00Z")
        );
    }

    private List<VideoChannel> seedChannels() {
        return List.of(
            new VideoChannel("ch-camera-a1", "dev-001", "camera", "Area A camera 01", "Main stream", "HIK_SDK", "rtsp://example/live/a1", "http://localhost:8088/live/camera-a1.flv", true, "online", "EDGE-01", 31.2312, 121.4741),
            new VideoChannel("ch-drone-001", "drone-001", "drone", "Dock drone 01", "Drone live stream", "DJI_SDK", "rtsp://example/live/drone-001", "http://localhost:8088/live/drone-001.flv", true, "online", "EDGE-01", 31.2321, 121.4752),
            new VideoChannel("ch-vehicle-front", "vehicle-001", "vehicle", "Hazmat vehicle 01", "Front camera", "JT1078", "jt1078://vehicle-001/front", "http://localhost:8088/live/vehicle-front.flv", true, "online", "EDGE-01", 31.2298, 121.4718)
        );
    }
}
