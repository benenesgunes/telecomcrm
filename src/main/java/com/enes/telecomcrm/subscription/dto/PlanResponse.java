package com.enes.telecomcrm.subscription.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Plan response")
public record PlanResponse(
		@Schema(description = "Plan id", example = "1")
		Long id,
		@Schema(description = "Plan name", example = "Home Internet 100Mbps")
		String name,
		@Schema(description = "Plan type", example = "INTERNET", allowableValues = {"MOBILE", "INTERNET", "TV"})
		String type,
		@Schema(description = "Monthly price", example = "399.99")
		BigDecimal monthlyPrice,
		@Schema(description = "Plan description", example = "100Mbps home internet package")
		String description
) {
}
