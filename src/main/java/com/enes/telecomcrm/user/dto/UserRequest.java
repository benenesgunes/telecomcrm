package com.enes.telecomcrm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
		@NotBlank @Size(min = 2, max = 100) String firstName,
		@NotBlank @Size(min = 2, max = 100) String lastName,
		@NotBlank @Email String email,
		@NotBlank
		@Pattern(
				regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
				message = "Password must be at least 8 chars with uppercase, digit, and special character"
		)
		String password
) {
}
