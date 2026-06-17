package com.aiplatform.inspection.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

@JsonTypeName("DRONE")
public class DroneDockNode extends DeviceNode {
    private String dockId;
    private String vendor;
    private String protocol = "DJI_CLOUD_API";
    private String endpoint;
    private int batteryPercent = 100;
    private String weather;
    private String edgeNodeId;
    private double latitude;
    private double longitude;
    private int droneCount;

    public DroneDockNode() { super(); this.deviceType = "DRONE"; }

    public DroneDockNode(String id, String name, String dockId, String vendor,
                         String endpoint, String edgeNodeId) {
        super(id, name, "DRONE", "offline", null);
        this.dockId = dockId;
        this.vendor = vendor;
        this.endpoint = endpoint;
        this.edgeNodeId = edgeNodeId;
    }

    public String getDockId() { return dockId; }
    public void setDockId(String dockId) { this.dockId = dockId; }
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public int getBatteryPercent() { return batteryPercent; }
    public void setBatteryPercent(int batteryPercent) { this.batteryPercent = batteryPercent; }
    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }
    public String getEdgeNodeId() { return edgeNodeId; }
    public void setEdgeNodeId(String edgeNodeId) { this.edgeNodeId = edgeNodeId; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getDroneCount() { return droneCount; }
    public void setDroneCount(int droneCount) { this.droneCount = droneCount; }

    public List<DeviceNode> getDrones() { return children; }
    public void setDrones(List<DeviceNode> drones) {
        this.children = drones;
        this.droneCount = drones != null ? drones.size() : 0;
    }
}
