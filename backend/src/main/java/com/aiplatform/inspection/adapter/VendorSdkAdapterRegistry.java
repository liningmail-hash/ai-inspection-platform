package com.aiplatform.inspection.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class VendorSdkAdapterRegistry {
    private final Map<String, CameraSdkAdapter> cameraAdapters;
    private final Map<String, VehicleVideoAdapter> vehicleAdapters;
    private final String cameraVendor;
    private final String droneVendor;
    private final String vehicleVendor;

    public VendorSdkAdapterRegistry(
        List<CameraSdkAdapter> cameraAdapters,
        List<VehicleVideoAdapter> vehicleAdapters,
        @Value("${platform.sdk.camera-vendor:MOCK_VENDOR}") String cameraVendor,
        @Value("${platform.sdk.drone-vendor:MOCK_VENDOR}") String droneVendor,
        @Value("${platform.sdk.vehicle-vendor:MOCK_VENDOR}") String vehicleVendor
    ) {
        this.cameraAdapters = cameraAdapters.stream()
            .collect(Collectors.toUnmodifiableMap(adapter -> normalize(adapter.vendor()), Function.identity()));
        this.vehicleAdapters = vehicleAdapters.stream()
            .collect(Collectors.toUnmodifiableMap(adapter -> normalize(adapter.vendor()), Function.identity()));
        this.cameraVendor = cameraVendor;
        this.droneVendor = droneVendor;
        this.vehicleVendor = vehicleVendor;
    }

    public CameraSdkAdapter cameraAdapter(String sourceType) {
        String configuredVendor = "drone".equals(sourceType) ? droneVendor : cameraVendor;
        return cameraAdapterByVendor(configuredVendor);
    }

    public VehicleVideoAdapter vehicleAdapter() {
        return vehicleAdapterByVendor(vehicleVendor);
    }

    public CameraSdkAdapter cameraAdapterByVendor(String vendor) {
        return cameraAdapters.getOrDefault(normalize(vendor), cameraAdapters.get(normalize("MOCK_VENDOR")));
    }

    public VehicleVideoAdapter vehicleAdapterByVendor(String vendor) {
        String normalized = normalize(vendor);
        if ("JT1078_GATEWAY".equals(normalized)) {
            normalized = "JT1078";
        }
        return vehicleAdapters.getOrDefault(normalized, vehicleAdapters.get(normalize("MOCK_VENDOR")));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
