package com.enes.telecomcrm.analytics.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Subscription dashboard metrics")
public record SubscriptionDashboardResponse(
		@Schema(description = "Most popular plans by active subscriber count")
		List<PlanPopularityDTO> mostPopularPlans,
		@Schema(description = "Monthly subscription growth keyed by month", example = "{\"2026-06\":5}")
		Map<String, Long> monthlyGrowth,
		@Schema(description = "Subscription status distribution", example = "{\"ACTIVE\":88,\"CANCELLED\":12}")
		Map<String, Long> statusDistribution
) {
}
