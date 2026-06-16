package com.enes.telecomcrm.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Admin dashboard metrics")
public record AdminDashboardResponse(
		@Schema(description = "Total user count", example = "125")
		long totalUsers,
		@Schema(description = "Active subscription count", example = "88")
		long activeSubscriptions,
		@Schema(description = "Cancelled subscription count", example = "12")
		long cancelledSubscriptions,
		@Schema(description = "Open ticket count", example = "9")
		long openTickets,
		@Schema(description = "Resolved ticket count", example = "42")
		long resolvedTickets
) {
}
