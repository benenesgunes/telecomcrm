package com.enes.telecomcrm.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login request")
public record LoginRequest(
		@Schema(description = "Registered email address", example = "john@example.com")
		@NotBlank @Email String email,
		@Schema(description = "Account password", example = "Secure@123")
		@NotBlank String password
) {
}
