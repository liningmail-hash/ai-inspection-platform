package com.aiplatform.inspection.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "deviceType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = NvrDevice.class, name = "NVR"),
    @JsonSubTypes.Type(value = ChannelNode.class, name = "CHANNEL"),
    @JsonSubTypes.Type(value = DroneDockNode.class, name = "DRONE"),
    @JsonSubTypes.Type(value = VehicleNode.class, name = "VEHICLE")
})
public abstract class DeviceNode {
    protected String id;
    protected String name;
    protected String deviceType;
    protected String status;
    protected String parentId;
    protected List<DeviceNode> children;
    protected Instant createdAt;
    protected Instant updatedAt;

    protected DeviceNode() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.children = new ArrayList<>();
    }

    protected DeviceNode(String id, String name, String deviceType, String status, String parentId) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.deviceType = deviceType;
        this.status = status != null ? status : "offline";
        this.parentId = parentId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.children = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public List<DeviceNode> getChildren() { return children; }
    public void setChildren(List<DeviceNode> children) { this.children = children; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public void addChild(DeviceNode child) {
        child.setParentId(this.id);
        this.children.add(child);
    }
}
