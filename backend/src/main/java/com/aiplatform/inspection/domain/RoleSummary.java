package com.aiplatform.inspection.domain;

import java.util.List;

public record RoleSummary(
    String id,
    String code,
    String name,
    String description,
    List<String> permissions,
    int userCount
) {
}
