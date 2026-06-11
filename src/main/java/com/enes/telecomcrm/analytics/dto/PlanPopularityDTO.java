package com.enes.telecomcrm.analytics.dto;

public record PlanPopularityDTO(
		Long planId,
		String planName,
		long activeSubscriberCount
) {
}
