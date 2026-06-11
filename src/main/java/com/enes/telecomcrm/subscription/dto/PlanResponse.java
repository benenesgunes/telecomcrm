package com.enes.telecomcrm.subscription.dto;

import java.math.BigDecimal;

public record PlanResponse(
		Long id,
		String name,
		String type,
		BigDecimal monthlyPrice,
		String description
) {
}
