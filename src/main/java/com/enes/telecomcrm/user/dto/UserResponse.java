package com.enes.telecomcrm.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
		Long id,
		String firstName,
		String lastName,
		String email,
		String role,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
