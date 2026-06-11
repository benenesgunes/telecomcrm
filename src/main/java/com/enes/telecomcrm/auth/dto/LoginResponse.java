package com.enes.telecomcrm.auth.dto;

public record LoginResponse(
		String token,
		String tokenType,
		long expiresIn
) {
}
