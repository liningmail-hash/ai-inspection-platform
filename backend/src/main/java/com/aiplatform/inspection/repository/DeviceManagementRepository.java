package com.aiplatform.inspection.repository;

import com.aiplatform.inspection.domain.Device;
import com.aiplatform.inspection.domain.VideoChannel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceManagementRepository {
    List<Device> devices();
    Optional<Device> deviceById(String id);
    Device createDevice(Device device);
    Optional<Device> updateDevice(String id, Device device);
    Optional<Device> updateDeviceStatus(String id, String status, Instant updatedAt);
    boolean deleteDevice(String id, Instant deletedAt);
    List<VideoChannel> replaceDeviceChannels(String deviceId, List<VideoChannel> channels, Instant syncedAt);
}
