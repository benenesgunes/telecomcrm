package com.enes.telecomcrm.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT login response")
public record LoginResponse(
		@Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
		String token,
		@Schema(description = "Token type", example = "Bearer")
		String tokenType,
		@Schema(description = "Expiration time in seconds", example = "86400")
		long expiresIn
) {
}
