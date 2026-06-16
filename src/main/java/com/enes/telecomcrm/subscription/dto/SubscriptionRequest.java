package com.enes.telecomcrm.subscription.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Subscription create request")
public record SubscriptionRequest(
		@Schema(description = "User id", example = "1")
		@NotNull Long userId,
		@Schema(description = "Plan id", example = "1")
		@NotNull Long planId,
		@Schema(description = "Subscription start date", example = "2026-06-16")
		@NotNull LocalDate startDate
) {
}
