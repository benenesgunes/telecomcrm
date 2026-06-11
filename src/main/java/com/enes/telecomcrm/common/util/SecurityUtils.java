package com.enes.telecomcrm.common.util;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static Optional<String> getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return Optional.empty();
		}

		return Optional.ofNullable(authentication.getName());
	}

	public static boolean hasRole(String role) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
		return authentication.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(normalizedRole::equals);
	}
}
