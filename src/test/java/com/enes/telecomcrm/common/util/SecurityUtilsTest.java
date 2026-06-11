package com.enes.telecomcrm.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilsTest {

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getCurrentUsernameReturnsAuthenticatedPrincipalName() {
		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken("user@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		assertThat(SecurityUtils.getCurrentUsername()).contains("user@example.com");
	}

	@Test
	void getCurrentUsernameReturnsEmptyWhenUnauthenticated() {
		assertThat(SecurityUtils.getCurrentUsername()).isEmpty();
	}

	@Test
	void hasRoleAcceptsRoleNamesWithOrWithoutPrefix() {
		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

		assertThat(SecurityUtils.hasRole("ADMIN")).isTrue();
		assertThat(SecurityUtils.hasRole("ROLE_ADMIN")).isTrue();
		assertThat(SecurityUtils.hasRole("USER")).isFalse();
	}
}
