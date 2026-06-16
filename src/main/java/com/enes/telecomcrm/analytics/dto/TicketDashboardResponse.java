package com.enes.telecomcrm.analytics.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ticket dashboard metrics")
public record TicketDashboardResponse(
		@Schema(description = "Ticket status distribution", example = "{\"OPEN\":9,\"RESOLVED\":42}")
		Map<String, Long> statusDistribution,
		@Schema(description = "Ticket priority distribution", example = "{\"LOW\":4,\"MEDIUM\":12,\"HIGH\":8}")
		Map<String, Long> priorityDistribution
) {
}
