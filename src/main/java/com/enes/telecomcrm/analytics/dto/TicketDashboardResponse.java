package com.enes.telecomcrm.analytics.dto;

import java.util.Map;

public record TicketDashboardResponse(
		Map<String, Long> statusDistribution,
		Map<String, Long> priorityDistribution
) {
}
