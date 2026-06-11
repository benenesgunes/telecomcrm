package com.enes.telecomcrm.subscription.dto;

import java.math.BigDecimal;

import com.enes.telecomcrm.subscription.entity.PlanType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlanRequest(
		@NotBlank @Size(max = 150) String name,
		@NotNull PlanType type,
		@NotNull @DecimalMin("0.01") BigDecimal monthlyPrice,
		String description
) {
}
