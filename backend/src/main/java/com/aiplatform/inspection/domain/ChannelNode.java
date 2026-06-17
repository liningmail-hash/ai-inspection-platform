package com.aiplatform.inspection.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("CHANNEL")
public class ChannelNode extends DeviceNode {
    private int channelNo;
    private String channelName;
    private String ptzType = "fixed";
    private String streamUrl;
    private String playUrl;
    private String resolution;
    private boolean aiEnabled;
    private double latitude;
    private double longitude;

    public ChannelNode() {
        super();
        this.deviceType = "CHANNEL";
    }

    public ChannelNode(String id, String name, String parentId, int channelNo,
                       String ptzType, String streamUrl, boolean aiEnabled) {
        super(id, name, "CHANNEL", "offline", parentId);
        this.channelNo = channelNo;
        this.channelName = name;
        this.ptzType = ptzType != null ? ptzType : "fixed";
        this.streamUrl = streamUrl;
        this.aiEnabled = aiEnabled;
    }

    public int getChannelNo() { return channelNo; }
    public void setChannelNo(int channelNo) { this.channelNo = channelNo; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getPtzType() { return ptzType; }
    public void setPtzType(String ptzType) { this.ptzType = ptzType; }
    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
    public String getPlayUrl() { return playUrl; }
    public void setPlayUrl(String playUrl) { this.playUrl = playUrl; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public boolean isAiEnabled() { return aiEnabled; }
    public void setAiEnabled(boolean aiEnabled) { this.aiEnabled = aiEnabled; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
