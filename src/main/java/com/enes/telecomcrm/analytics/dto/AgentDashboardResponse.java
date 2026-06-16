package com.enes.telecomcrm.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Support agent dashboard metrics")
public record AgentDashboardResponse(
		@Schema(description = "Agent user id", example = "2")
		Long agentId,
		@Schema(description = "Agent full name", example = "Support Agent")
		String agentName,
		@Schema(description = "Assigned ticket count", example = "15")
		long assignedTickets,
		@Schema(description = "Resolved ticket count", example = "11")
		long resolvedTickets,
		@Schema(description = "Average resolution time in hours", example = "5.5")
		double averageResolutionTimeHours
) {
}
