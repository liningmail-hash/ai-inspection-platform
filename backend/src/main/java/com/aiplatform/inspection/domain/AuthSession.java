package com.aiplatform.inspection.domain;

public record AuthSession(
    String token,
    UserAccount user,
    long expiresInSeconds
) {
}
