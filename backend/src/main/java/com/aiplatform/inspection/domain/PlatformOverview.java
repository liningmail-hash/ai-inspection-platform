package com.aiplatform.inspection.domain;

import java.util.List;

public record PlatformOverview(List<Kpi> kpis, List<AlarmEvent> alarms, List<InspectionPlan> plans, DroneDock dock) {}
