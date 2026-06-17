package com.aiplatform.inspection.adapter;

public interface DroneAdapter {
    String vendor();
    DockStatus getDockStatus(String dockId);
    DroneStatus getDroneStatus(String droneId);
    AdapterResult uploadRoute(String dockId, FlightRoute route);
    AdapterResult startTask(String routeId);
    AdapterResult stopTask(String taskId);
    String getVideoStreamUrl(String droneId);
}
