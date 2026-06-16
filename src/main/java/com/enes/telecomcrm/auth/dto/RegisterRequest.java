package com.enes.telecomcrm.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Registration request for a new customer account")
public record RegisterRequest(
		@Schema(description = "User first name", example = "John")
		@NotBlank @Size(min = 2, max = 100) String firstName,
		@Schema(description = "User last name", example = "Doe")
		@NotBlank @Size(min = 2, max = 100) String lastName,
		@Schema(description = "Unique user email address", example = "john@example.com")
		@NotBlank @Email String email,
		@Schema(description = "Password with uppercase, digit, and special character", example = "Secure@123")
		@NotBlank
		@Pattern(
				regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
				message = "Password must be at least 8 chars with uppercase, digit, and special character"
		)
		String password
) {
}
