package com.enes.telecomcrm.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Popular plan metric")
public record PlanPopularityDTO(
		@Schema(description = "Plan id", example = "1")
		Long planId,
		@Schema(description = "Plan name", example = "Home Internet 100Mbps")
		String planName,
		@Schema(description = "Active subscriber count", example = "42")
		long activeSubscriberCount
) {
}
