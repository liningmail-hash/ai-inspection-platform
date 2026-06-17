package com.aiplatform.inspection.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

@JsonTypeName("NVR")
public class NvrDevice extends DeviceNode {
    private String gbDeviceId;
    private String gbPassword;
    private String sipHost;
    private int sipPort = 5060;
    private String protocol = "GB28181";
    private String vendor;
    private String location;
    private String edgeNodeId;
    private int channelCount;

    public NvrDevice() {
        super();
        this.deviceType = "NVR";
    }

    public NvrDevice(String id, String name, String gbDeviceId, String gbPassword,
                     String sipHost, int sipPort, String vendor, String location) {
        super(id, name, "NVR", "offline", null);
        this.gbDeviceId = gbDeviceId;
        this.gbPassword = gbPassword;
        this.sipHost = sipHost;
        this.sipPort = sipPort > 0 ? sipPort : 5060;
        this.vendor = vendor;
        this.location = location;
    }

    public String getGbDeviceId() { return gbDeviceId; }
    public void setGbDeviceId(String gbDeviceId) { this.gbDeviceId = gbDeviceId; }
    public String getGbPassword() { return gbPassword; }
    public void setGbPassword(String gbPassword) { this.gbPassword = gbPassword; }
    public String getSipHost() { return sipHost; }
    public void setSipHost(String sipHost) { this.sipHost = sipHost; }
    public int getSipPort() { return sipPort; }
    public void setSipPort(int sipPort) { this.sipPort = sipPort; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
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
