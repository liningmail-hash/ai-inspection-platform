package com.aiplatform.inspection.domain;

import java.util.List;

public record UserAccount(
    String id,
    String username,
    String displayName,
    String organization,
    String site,
    String status,
    List<String> roles,
    List<String> permissions
) {
}
