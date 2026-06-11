package com.enes.telecomcrm.analytics.dto;

import java.util.List;
import java.util.Map;

public record SubscriptionDashboardResponse(
		List<PlanPopularityDTO> mostPopularPlans,
		Map<String, Long> monthlyGrowth,
		Map<String, Long> statusDistribution
) {
}
