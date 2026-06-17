package com.aiplatform.inspection.adapter;

import java.util.List;

public interface DeviceAdapter {
    String protocol();
    AdapterResult register(DeviceRegistration registration);
    AdapterResult heartbeat(String deviceId);
    StreamDescriptor openStream(String channelId, String profile);
    AdapterResult capture(String channelId);
    AdapterResult ptz(String channelId, String command, int speed);
    List<RecordSegment> queryRecords(String channelId, String beginTime, String endTime);
}
