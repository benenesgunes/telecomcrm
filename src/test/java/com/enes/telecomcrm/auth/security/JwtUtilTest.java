package com.enes.telecomcrm.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;

class JwtUtilTest {

	private final JwtUtil jwtUtil = new JwtUtil(
			"test-jwt-secret-value-that-is-long-enough-for-hs256",
			86400000
	);

	@Test
	void generateToken_createsValidBearerJwtForUser() {
		User user = User.builder()
				.id(42L)
				.email("john@example.com")
				.password("hashed-password")
				.role(Role.ROLE_USER)
				.build();

		String token = jwtUtil.generateToken(user);
		UserDetails userDetails = org.springframework.security.core.userdetails.User
				.withUsername("john@example.com")
				.password("hashed-password")
				.authorities("ROLE_USER")
				.build();

		assertNotNull(token);
		assertEquals(3, token.split("\\.").length);
		assertEquals("john@example.com", jwtUtil.extractUsername(token));
		assertTrue(jwtUtil.isTokenValid(token, userDetails));
		assertEquals(86400L, jwtUtil.getExpirationSeconds());
	}
}
