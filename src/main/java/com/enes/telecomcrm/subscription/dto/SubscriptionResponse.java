package com.enes.telecomcrm.subscription.dto;

import java.time.LocalDate;

import com.enes.telecomcrm.user.dto.UserResponse;

public record SubscriptionResponse(
		Long id,
		String status,
		LocalDate startDate,
		LocalDate endDate,
		UserResponse user,
		PlanResponse plan
) {
}
