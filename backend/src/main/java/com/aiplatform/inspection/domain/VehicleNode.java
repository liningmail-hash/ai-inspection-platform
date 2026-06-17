package com.aiplatform.inspection.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

@JsonTypeName("VEHICLE")
public class VehicleNode extends DeviceNode {
    private String plateNo;
    private String vehicleType;
    private String vendor;
    private String protocol = "JT1078";
    private String endpoint;
    private double speedKph;
    private double latitude;
    private double longitude;
    private String edgeNodeId;
    private int channelCount;

    public VehicleNode() { super(); this.deviceType = "VEHICLE"; }

    public VehicleNode(String id, String name, String plateNo, String vehicleType,
                       String vendor, String endpoint, String edgeNodeId) {
        super(id, name, "VEHICLE", "offline", null);
        this.plateNo = plateNo;
        this.vehicleType = vehicleType;
        this.vendor = vendor;
        this.endpoint = endpoint;
        this.edgeNodeId = edgeNodeId;
    }

    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public double getSpeedKph() { return speedKph; }
    public void setSpeedKph(double speedKph) { this.speedKph = speedKph; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getEdgeNodeId() { return edgeNodeId; }
    public void setEdgeNodeId(String edgeNodeId) { this.edgeNodeId = edgeNodeId; }
    public int getChannelCount() { return channelCount; }
    public void setChannelCount(int channelCount) { this.channelCount = channelCount; }

    public List<DeviceNode> getChannels() { return children; }
    public void setChannels(List<DeviceNode> channels) {
        this.children = channels;
        this.channelCount = channels != null ? channels.size() : 0;
    }
}
