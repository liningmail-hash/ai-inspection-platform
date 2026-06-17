package com.aiplatform.inspection.adapter;

import java.util.Map;

public record AdapterResult(boolean success, String message, Map<String, Object> data) {}
