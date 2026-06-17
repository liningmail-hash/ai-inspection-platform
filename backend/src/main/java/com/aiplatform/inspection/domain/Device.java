package com.aiplatform.inspection.domain;

public record Device(
    String id,
    String name,
    String sourceType,
    String vendor,
    String protocol,
    String endpoint,
    String credentialRef,
    String location,
    String edgeNodeId,
    String status,
    String streamUrl,
    String createdAt,
    String updatedAt
) {
    public Device(String id, String name, String vendor, String protocol, String status, String streamUrl) {
        this(id, name, "camera", vendor, protocol, streamUrl, "", "", "", status, streamUrl, "", "");
    }
}
