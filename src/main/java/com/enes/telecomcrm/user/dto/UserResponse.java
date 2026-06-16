package com.enes.telecomcrm.user.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User response")
public record UserResponse(
		@Schema(description = "User id", example = "1")
		Long id,
		@Schema(description = "User first name", example = "John")
		String firstName,
		@Schema(description = "User last name", example = "Doe")
		String lastName,
		@Schema(description = "User email address", example = "john@example.com")
		String email,
		@Schema(description = "User role", example = "ROLE_USER", allowableValues = {"ROLE_USER", "ROLE_SUPPORT_AGENT", "ROLE_ADMIN"})
		String role,
		@Schema(description = "Creation timestamp", example = "2026-06-16T10:15:30")
		LocalDateTime createdAt,
		@Schema(description = "Last update timestamp", example = "2026-06-16T10:20:30")
		LocalDateTime updatedAt
) {
}
