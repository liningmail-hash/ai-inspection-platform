package com.aiplatform.inspection.repository;

import com.aiplatform.inspection.domain.Device;
import com.aiplatform.inspection.domain.DeviceNode;
import com.aiplatform.inspection.domain.NvrDevice;
import com.aiplatform.inspection.domain.ChannelNode;
import com.aiplatform.inspection.domain.DroneDockNode;
import com.aiplatform.inspection.domain.VehicleNode;
import com.aiplatform.inspection.domain.VideoChannel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceManagementRepository {
    // Legacy — keep for backward compatibility
    List<Device> devices();
    Optional<Device> deviceById(String id);
    Device createDevice(Device device);
    Optional<Device> updateDevice(String id, Device device);
    Optional<Device> updateDeviceStatus(String id, String status, Instant updatedAt);
    boolean deleteDevice(String id, Instant deletedAt);
    List<VideoChannel> replaceDeviceChannels(String deviceId, List<VideoChannel> channels, Instant syncedAt);

    // New — hierarchical device management v2
    List<DeviceNode> deviceTree();
    Optional<DeviceNode> deviceNodeById(String id);
    List<DeviceNode> deviceNodesByType(String deviceType);

    // NVR
    List<NvrDevice> nvrDevices();
    Optional<NvrDevice> nvrDeviceById(String id);
    NvrDevice createNvrDevice(NvrDevice nvr);
    Optional<NvrDevice> updateNvrDevice(String id, NvrDevice nvr);
    boolean deleteNvrDevice(String id);
    List<ChannelNode> syncNvrChannels(String nvrId, List<ChannelNode> channels);

    // Drone
    List<DroneDockNode> droneDocks();
    Optional<DroneDockNode> droneDockById(String id);
    DroneDockNode createDroneDock(DroneDockNode dock);
    Optional<DroneDockNode> updateDroneDock(String id, DroneDockNode dock);
    boolean deleteDroneDock(String id);

    // Vehicle
    List<VehicleNode> vehicles();
    Optional<VehicleNode> vehicleById(String id);
    VehicleNode createVehicle(VehicleNode vehicle);
    Optional<VehicleNode> updateVehicle(String id, VehicleNode vehicle);
    boolean deleteVehicle(String id);
}
