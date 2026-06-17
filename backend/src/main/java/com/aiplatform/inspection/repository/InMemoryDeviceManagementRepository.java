package com.aiplatform.inspection.repository;

import com.aiplatform.inspection.domain.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("!postgres")
public class InMemoryDeviceManagementRepository implements DeviceManagementRepository {

    private final Map<String, DeviceNode> deviceNodes = new ConcurrentHashMap<>();
    private final Map<String, List<ChannelNode>> nvrChannels = new ConcurrentHashMap<>();
    private final Map<String, Device> legacyDevices = new ConcurrentHashMap<>();
    private final Map<String, VideoChannel> legacyChannels = new ConcurrentHashMap<>();

    public InMemoryDeviceManagementRepository() {
        seedDeviceNodes();
        seedLegacyDevices();
    }

    // ========== v2 Methods ==========

    @Override
    public List<DeviceNode> deviceTree() {
        List<DeviceNode> roots = new ArrayList<>();
        for (DeviceNode node : deviceNodes.values()) {
            if (node.getParentId() == null && !"deleted".equals(node.getStatus())) {
                DeviceNode root = buildTree(node);
                roots.add(root);
            }
        }
        return roots;
    }

    private DeviceNode buildTree(DeviceNode node) {
        node.setChildren(new ArrayList<>());
        for (DeviceNode child : deviceNodes.values()) {
            if (node.getId().equals(child.getParentId()) && !"deleted".equals(child.getStatus())) {
                node.addChild(child);
            }
        }
        return node;
    }

    @Override
    public Optional<DeviceNode> deviceNodeById(String id) {
        return Optional.ofNullable(deviceNodes.get(id)).filter(n -> !"deleted".equals(n.getStatus()));
    }

    @Override
    public List<DeviceNode> deviceNodesByType(String deviceType) {
        return deviceNodes.values().stream()
            .filter(n -> deviceType.equals(n.getDeviceType()) && !"deleted".equals(n.getStatus())).toList();
    }

    @Override
    public List<NvrDevice> nvrDevices() {
        return deviceNodes.values().stream()
            .filter(n -> n instanceof NvrDevice && !"deleted".equals(n.getStatus()))
            .map(n -> {
                NvrDevice nvr = (NvrDevice) n;
                List<ChannelNode> channels = nvrChannels.getOrDefault(nvr.getId(), List.of());
                nvr.setChannels(new ArrayList<>(channels));
                return nvr;
            }).toList();
    }

    @Override
    public Optional<NvrDevice> nvrDeviceById(String id) {
        DeviceNode node = deviceNodes.get(id);
        if (node instanceof NvrDevice nvr && !"deleted".equals(nvr.getStatus())) {
            List<ChannelNode> channels = nvrChannels.getOrDefault(id, List.of());
            nvr.setChannels(new ArrayList<>(channels));
            return Optional.of(nvr);
        }
        return Optional.empty();
    }

    @Override
    public NvrDevice createNvrDevice(NvrDevice nvr) {
        nvr.setDeviceType("NVR");
        nvr.setCreatedAt(Instant.now());
        nvr.setUpdatedAt(Instant.now());
        if (nvr.getId() == null) nvr.setId(UUID.randomUUID().toString());
        deviceNodes.put(nvr.getId(), nvr);
        nvrChannels.putIfAbsent(nvr.getId(), new ArrayList<>());
        return nvr;
    }

    @Override
    public Optional<NvrDevice> updateNvrDevice(String id, NvrDevice nvr) {
        if (!deviceNodes.containsKey(id)) return Optional.empty();
        nvr.setId(id);
        nvr.setUpdatedAt(Instant.now());
        deviceNodes.put(id, nvr);
        return Optional.of(nvr);
    }

    @Override
    public boolean deleteNvrDevice(String id) {
        DeviceNode node = deviceNodes.get(id);
        if (node == null) return false;
        node.setStatus("deleted");
        node.setUpdatedAt(Instant.now());
        nvrChannels.remove(id);
        return true;
    }

    @Override
    public List<ChannelNode> syncNvrChannels(String nvrId, List<ChannelNode> channels) {
        List<ChannelNode> updated = new ArrayList<>();
        for (ChannelNode ch : channels) {
            ch.setParentId(nvrId);
            ch.setDeviceType("CHANNEL");
            ch.setUpdatedAt(Instant.now());
            if (ch.getId() == null) ch.setId(UUID.randomUUID().toString());
            deviceNodes.put(ch.getId(), ch);
            updated.add(ch);
        }
        nvrChannels.put(nvrId, updated);
        DeviceNode node = deviceNodes.get(nvrId);
        if (node instanceof NvrDevice n) {
            n.setChannelCount(updated.size());
            n.setUpdatedAt(Instant.now());
        }
        return updated;
    }

    @Override
    public List<DroneDockNode> droneDocks() {
        return deviceNodes.values().stream()
            .filter(n -> n instanceof DroneDockNode && !"deleted".equals(n.getStatus()))
            .map(n -> (DroneDockNode) n).toList();
    }

    @Override
    public Optional<DroneDockNode> droneDockById(String id) {
        DeviceNode node = deviceNodes.get(id);
        if (node instanceof DroneDockNode dock && !"deleted".equals(dock.getStatus())) return Optional.of(dock);
        return Optional.empty();
    }

    @Override
    public DroneDockNode createDroneDock(DroneDockNode dock) {
        dock.setDeviceType("DRONE");
        dock.setCreatedAt(Instant.now());
        dock.setUpdatedAt(Instant.now());
        if (dock.getId() == null) dock.setId(UUID.randomUUID().toString());
        deviceNodes.put(dock.getId(), dock);
        return dock;
    }

    @Override
    public Optional<DroneDockNode> updateDroneDock(String id, DroneDockNode dock) {
        if (!deviceNodes.containsKey(id)) return Optional.empty();
        dock.setId(id);
        dock.setUpdatedAt(Instant.now());
        deviceNodes.put(id, dock);
        return Optional.of(dock);
    }

    @Override
    public boolean deleteDroneDock(String id) {
        DeviceNode node = deviceNodes.get(id);
        if (node == null) return false;
        node.setStatus("deleted");
        node.setUpdatedAt(Instant.now());
        return true;
    }

    @Override
    public List<VehicleNode> vehicles() {
        return deviceNodes.values().stream()
            .filter(n -> n instanceof VehicleNode && !"deleted".equals(n.getStatus()))
            .map(n -> {
                VehicleNode v = (VehicleNode) n;
                List<ChannelNode> channels = nvrChannels.getOrDefault(v.getId(), List.of());
                v.setChannels(new ArrayList<>(channels));
                return v;
            }).toList();
    }

    @Override
    public Optional<VehicleNode> vehicleById(String id) {
        DeviceNode node = deviceNodes.get(id);
        if (node instanceof VehicleNode v && !"deleted".equals(v.getStatus())) {
            List<ChannelNode> channels = nvrChannels.getOrDefault(id, List.of());
            v.setChannels(new ArrayList<>(channels));
            return Optional.of(v);
        }
        return Optional.empty();
    }

    @Override
    public VehicleNode createVehicle(VehicleNode vehicle) {
        vehicle.setDeviceType("VEHICLE");
        vehicle.setCreatedAt(Instant.now());
        vehicle.setUpdatedAt(Instant.now());
        if (vehicle.getId() == null) vehicle.setId(UUID.randomUUID().toString());
        deviceNodes.put(vehicle.getId(), vehicle);
        nvrChannels.putIfAbsent(vehicle.getId(), new ArrayList<>());
        return vehicle;
    }

    @Override
    public Optional<VehicleNode> updateVehicle(String id, VehicleNode vehicle) {
        if (!deviceNodes.containsKey(id)) return Optional.empty();
        vehicle.setId(id);
        vehicle.setUpdatedAt(Instant.now());
        deviceNodes.put(id, vehicle);
        return Optional.of(vehicle);
    }

    @Override
    public boolean deleteVehicle(String id) {
        DeviceNode node = deviceNodes.get(id);
        if (node == null) return false;
        node.setStatus("deleted");
        node.setUpdatedAt(Instant.now());
        nvrChannels.remove(id);
        return true;
    }

    // ========== v1 Legacy Methods ==========

    @Override
    public List<Device> devices() {
        if (legacyDevices.isEmpty()) return List.of();
        return legacyDevices.values().stream().filter(d -> !"deleted".equals(d.status()))
            .sorted((a, b) -> b.createdAt().compareTo(a.createdAt())).toList();
    }

    @Override
    public Optional<Device> deviceById(String id) {
        return Optional.ofNullable(legacyDevices.get(id)).filter(d -> !"deleted".equals(d.status()));
    }

    @Override
    public Device createDevice(Device device) {
        legacyDevices.put(device.id(), device);
        return device;
    }

    @Override
    public Optional<Device> updateDevice(String id, Device device) {
        if (!legacyDevices.containsKey(id) || "deleted".equals(legacyDevices.get(id).status()))
            return Optional.empty();
        legacyDevices.put(id, device);
        return Optional.of(device);
    }

    @Override
    public Optional<Device> updateDeviceStatus(String id, String status, Instant updatedAt) {
        Device current = legacyDevices.get(id);
        if (current == null || "deleted".equals(current.status())) return Optional.empty();
        Device updated = new Device(current.id(), current.name(), current.sourceType(), current.vendor(),
            current.protocol(), current.endpoint(), current.credentialRef(), current.location(),
            current.edgeNodeId(), status, current.streamUrl(), current.createdAt(), updatedAt.toString());
        legacyDevices.put(id, updated);
        return Optional.of(updated);
    }

    @Override
    public boolean deleteDevice(String id, Instant deletedAt) {
        Device current = legacyDevices.get(id);
        if (current == null || "deleted".equals(current.status())) return false;
        legacyDevices.put(id, new Device(current.id(), current.name(), current.sourceType(), current.vendor(),
            current.protocol(), current.endpoint(), current.credentialRef(), current.location(),
            current.edgeNodeId(), "deleted", current.streamUrl(), current.createdAt(), deletedAt.toString()));
        return true;
    }

    @Override
    public List<VideoChannel> replaceDeviceChannels(String deviceId, List<VideoChannel> channels, Instant syncedAt) {
        legacyChannels.entrySet().removeIf(e -> e.getValue().sourceId().equals(deviceId));
        channels.forEach(c -> legacyChannels.put(c.id(), c));
        return channels;
    }

    // ========== Seed Data ==========

    private void seedDeviceNodes() {
        NvrDevice nvr1 = new NvrDevice("nvr-001", "厂区一号NVR（海康）", "34020000001320000001", "admin123", "192.168.1.100", 5060, "HIKVISION", "A区危化仓机房");
        nvr1.setStatus("online");
        nvr1.setEdgeNodeId("EDGE-01");
        createNvrDevice(nvr1);
        ChannelNode ch11 = new ChannelNode("ch-nvr1-01", "A区危化仓-枪机01", "nvr-001", 1, "fixed", "rtsp://192.168.1.100:554/Streaming/Channels/101", true);
        ch11.setStatus("online");
        ChannelNode ch12 = new ChannelNode("ch-nvr1-02", "A区危化仓-球机01", "nvr-001", 2, "ptz", "rtsp://192.168.1.100:554/Streaming/Channels/201", true);
        ch12.setStatus("online");
        ChannelNode ch13 = new ChannelNode("ch-nvr1-03", "A区围界-红外枪机01", "nvr-001", 3, "fixed", "rtsp://192.168.1.100:554/Streaming/Channels/301", false);
        List<ChannelNode> nvr1Channels = new ArrayList<>();
        nvr1Channels.add(ch11);
        nvr1Channels.add(ch12);
        nvr1Channels.add(ch13);
        syncNvrChannels("nvr-001", nvr1Channels);

        NvrDevice nvr2 = new NvrDevice("nvr-002", "东区围界NVR（大华）", "34020000001320000002", "admin456", "192.168.1.101", 5060, "DAHUA", "东区围界机房");
        nvr2.setStatus("online");
        nvr2.setEdgeNodeId("EDGE-01");
        createNvrDevice(nvr2);
        ChannelNode ch21 = new ChannelNode("ch-nvr2-01", "东区围界-球机03", "nvr-002", 1, "ptz", "rtsp://192.168.1.101:554/cam/realmonitor?channel=1", true);
        ch21.setStatus("online");
        ChannelNode ch22 = new ChannelNode("ch-nvr2-02", "东区大门-枪机01", "nvr-002", 2, "fixed", "rtsp://192.168.1.101:554/cam/realmonitor?channel=2", false);
        ch22.setStatus("online");
        List<ChannelNode> nvr2Channels = new ArrayList<>();
        nvr2Channels.add(ch21);
        nvr2Channels.add(ch22);
        syncNvrChannels("nvr-002", nvr2Channels);

        NvrDevice nvr3 = new NvrDevice("nvr-003", "C区装卸口NVR（预留）", "", "", "", 5060, "", "C区装卸口弱电间");
        createNvrDevice(nvr3);

        DroneDockNode dock = new DroneDockNode("drone-001", "一号无人机机场", "dock-001", "DJI_DOCK", "https://dji-dock.local", "EDGE-01");
        dock.setStatus("ready");
        dock.setBatteryPercent(87);
        dock.setWeather("风速 3.1m/s / 小雨");
        dock.setLatitude(31.2321);
        dock.setLongitude(121.4752);
        createDroneDock(dock);

        VehicleNode vehicle = new VehicleNode("vehicle-001", "危化品转运车01", "沪A-D8123", "危化品转运车", "JT1078", "https://jt1078-gateway.local", "EDGE-01");
        vehicle.setStatus("online");
        vehicle.setSpeedKph(36.5);
        vehicle.setLatitude(31.2298);
        vehicle.setLongitude(121.4718);
        createVehicle(vehicle);
        ChannelNode vch1 = new ChannelNode("ch-veh-01", "车头摄像头", "vehicle-001", 1, "fixed", "jt1078://vehicle-001/front", true);
        vch1.setStatus("online");
        ChannelNode vch2 = new ChannelNode("ch-veh-02", "车厢摄像头", "vehicle-001", 2, "fixed", "jt1078://vehicle-001/cargo", false);
        vch2.setStatus("online");
        List<ChannelNode> vChannels = new ArrayList<>();
        vChannels.add(vch1);
        vChannels.add(vch2);
        nvrChannels.put("vehicle-001", vChannels);
    }

    private void seedLegacyDevices() {
        legacyDevices.put("dev-001", new Device("dev-001", "Area A camera 01", "camera", "HIKVISION", "GB28181",
            "rtsp://example/live/a1", "", "A区危化仓", "EDGE-01", "online",
            "rtsp://example/live/a1", "2026-06-16T09:00:00Z", "2026-06-16T09:00:00Z"));
    }
}
