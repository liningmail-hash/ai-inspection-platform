package com.aiplatform.inspection.adapter;

import com.aiplatform.inspection.domain.IntegrationConfig;
import com.aiplatform.inspection.domain.VideoChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class HikvisionCameraSdkAdapter implements CameraSdkAdapter {
    private final String endpoint;
    private final String username;
    private final String credentialRef;
    private final String sdkLibraryPath;
    private final String streamMode;
    private final String callbackBaseUrl;

    public HikvisionCameraSdkAdapter(
        @Value("${platform.sdk.hikvision.endpoint:}") String endpoint,
        @Value("${platform.sdk.hikvision.username:}") String username,
        @Value("${platform.sdk.hikvision.credential-ref:}") String credentialRef,
        @Value("${platform.sdk.hikvision.sdk-library-path:}") String sdkLibraryPath,
        @Value("${platform.sdk.hikvision.stream-mode:rtsp}") String streamMode,
        @Value("${platform.sdk.hikvision.callback-base-url:}") String callbackBaseUrl
    ) {
        this.endpoint = endpoint;
        this.username = username;
        this.credentialRef = credentialRef;
        this.sdkLibraryPath = sdkLibraryPath;
        this.streamMode = streamMode;
        this.callbackBaseUrl = callbackBaseUrl;
    }

    @Override
    public String vendor() {
        return "HIKVISION";
    }

    @Override
    public AdapterResult testConnection(IntegrationConfig config) {
        List<String> missing = missingConfiguration(config);
        if (!missing.isEmpty()) {
            return unavailable("HIKVISION connection test unavailable: missing " + String.join(", ", missing), config, missing);
        }
        return unavailable("HIKVISION connection test unavailable: HCNetSDK Java wrapper and native libraries are not installed", config, List.of("HCNetSDK jar", "HCNetSDK native libraries"));
    }

    @Override
    public List<VideoChannel> syncChannels(IntegrationConfig config) {
        if (!missingConfiguration(config).isEmpty()) {
            return List.of();
        }
        // Real SDK mapping target:
        // deviceId -> sourceId, channelId -> id, channelName -> name,
        // streamType/main-sub code -> streamName, streamUrl -> rawUrl/playUrl,
        // online flag -> online/status, device location -> latitude/longitude.
        return List.of();
    }

    @Override
    public StreamDescriptor openPreview(String channelId, String protocol) {
        throw new IllegalStateException("HIKVISION preview unavailable: HCNetSDK preview API is not wired and endpoint/credentialRef/sdkLibraryPath/streamMode must be configured");
    }

    @Override
    public AdapterResult snapshot(String channelId) {
        return operationUnavailable("HIKVISION snapshot unavailable: HCNetSDK snapshot API is not wired and sdkLibraryPath/credentialRef must be configured", channelId, "snapshot");
    }

    @Override
    public AdapterResult record(String channelId, int seconds) {
        return operationUnavailable("HIKVISION recording unavailable: HCNetSDK record API is not wired and sdkLibraryPath/callbackBaseUrl must be configured", channelId, "record");
    }

    private List<String> missingConfiguration(IntegrationConfig config) {
        return List.of(
            missing("endpoint", effectiveEndpoint(config)),
            missing("username", username),
            missing("credentialRef", effectiveCredentialRef(config)),
            missing("sdkLibraryPath", sdkLibraryPath),
            missing("streamMode", streamMode),
            missing("callbackBaseUrl", callbackBaseUrl)
        ).stream().filter(value -> !value.isBlank()).toList();
    }

    private String missing(String name, String value) {
        return value == null || value.isBlank() ? name : "";
    }

    private AdapterResult unavailable(String message, IntegrationConfig config, List<String> missing) {
        return new AdapterResult(false, message, Map.of(
            "vendor", vendor(),
            "endpoint", effectiveEndpoint(config),
            "credentialRef", effectiveCredentialRef(config),
            "streamMode", safe(streamMode),
            "callbackBaseUrl", safe(callbackBaseUrl),
            "missing", missing,
            "sdkRequired", true
        ));
    }

    private AdapterResult operationUnavailable(String message, String channelId, String operation) {
        return new AdapterResult(false, message, Map.of(
            "vendor", vendor(),
            "channelId", channelId,
            "operation", operation,
            "sdkLibraryPath", safe(sdkLibraryPath),
            "streamMode", safe(streamMode),
            "callbackBaseUrl", safe(callbackBaseUrl),
            "sdkRequired", true
        ));
    }

    private String effectiveEndpoint(IntegrationConfig config) {
        return firstConfigured(config.endpoint(), endpoint);
    }

    private String effectiveCredentialRef(IntegrationConfig config) {
        return firstConfigured(config.credentialRef(), credentialRef);
    }

    private String firstConfigured(String primary, String fallback) {
        return primary == null || primary.isBlank() ? safe(fallback) : primary;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
