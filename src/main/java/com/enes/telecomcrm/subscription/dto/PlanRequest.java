package com.enes.telecomcrm.subscription.dto;

import java.math.BigDecimal;

import com.enes.telecomcrm.subscription.entity.PlanType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Plan create or update request")
public record PlanRequest(
		@Schema(description = "Plan name", example = "Home Internet 100Mbps")
		@NotBlank @Size(max = 150) String name,
		@Schema(description = "Plan type", example = "INTERNET", allowableValues = {"MOBILE", "INTERNET", "TV"})
		@NotNull PlanType type,
		@Schema(description = "Monthly price", example = "399.99")
		@NotNull @DecimalMin("0.01") BigDecimal monthlyPrice,
		@Schema(description = "Plan description", example = "100Mbps home internet package")
		String description
) {
}
