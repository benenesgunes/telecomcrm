package com.enes.telecomcrm.analytics.dto;

public record AgentDashboardResponse(
		Long agentId,
		String agentName,
		long assignedTickets,
		long resolvedTickets,
		double averageResolutionTimeHours
) {
}
