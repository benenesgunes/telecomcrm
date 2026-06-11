package com.enes.telecomcrm.subscription.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record SubscriptionRequest(
		@NotNull Long userId,
		@NotNull Long planId,
		@NotNull LocalDate startDate
) {
}
