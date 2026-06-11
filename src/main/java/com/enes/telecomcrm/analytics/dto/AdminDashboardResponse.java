package com.enes.telecomcrm.analytics.dto;

public record AdminDashboardResponse(
		long totalUsers,
		long activeSubscriptions,
		long cancelledSubscriptions,
		long openTickets,
		long resolvedTickets
) {
}
