package com.enes.telecomcrm.subscription.dto;

import java.time.LocalDate;

import com.enes.telecomcrm.user.dto.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Subscription response")
public record SubscriptionResponse(
		@Schema(description = "Subscription id", example = "1")
		Long id,
		@Schema(description = "Subscription status", example = "ACTIVE", allowableValues = {"ACTIVE", "SUSPENDED", "CANCELLED", "EXPIRED"})
		String status,
		@Schema(description = "Start date", example = "2026-06-16")
		LocalDate startDate,
		@Schema(description = "End date", example = "2027-06-16")
		LocalDate endDate,
		@Schema(description = "Subscribed user")
		UserResponse user,
		@Schema(description = "Subscribed plan")
		PlanResponse plan
) {
}
