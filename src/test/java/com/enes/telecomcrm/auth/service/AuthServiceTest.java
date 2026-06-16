package com.enes.telecomcrm.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.enes.telecomcrm.auth.dto.LoginRequest;
import com.enes.telecomcrm.auth.dto.LoginResponse;
import com.enes.telecomcrm.auth.dto.RegisterRequest;
import com.enes.telecomcrm.auth.security.JwtUtil;
import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.search.service.UserSearchIndexService;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.mapper.UserMapper;
import com.enes.telecomcrm.user.repository.UserRepository;

class AuthServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final UserMapper userMapper = mock(UserMapper.class);
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
	private final JwtUtil jwtUtil = mock(JwtUtil.class);
	private final UserSearchIndexService userSearchIndexService = mock(UserSearchIndexService.class);
	private final AuthService authService = new AuthService(
			userRepository,
			userMapper,
			passwordEncoder,
			jwtUtil,
			userSearchIndexService
	);

	@Test
	void register_hashesPasswordAssignsUserRoleAndReturnsUserResponse() {
		RegisterRequest request = new RegisterRequest("John", "Doe", "JOHN@example.com", "Secure@123");
		User mappedUser = User.builder()
				.firstName("John")
				.lastName("Doe")
				.email("JOHN@example.com")
				.password("Secure@123")
				.build();
		UserResponse expectedResponse = new UserResponse(1L, "John", "Doe", "john@example.com", "ROLE_USER", null, null);

		when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
		when(userMapper.toEntity(request)).thenReturn(mappedUser);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

		UserResponse response = authService.register(request);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();
		verify(userSearchIndexService).index(savedUser);

		assertEquals(expectedResponse, response);
		assertEquals("john@example.com", savedUser.getEmail());
		assertEquals(Role.ROLE_USER, savedUser.getRole());
		assertNotEquals("Secure@123", savedUser.getPassword());
		assertTrue(passwordEncoder.matches("Secure@123", savedUser.getPassword()));
	}

	@Test
	void register_whenEmailExistsThrowsBusinessRuleException() {
		RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "Secure@123");
		when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

		assertThrows(BusinessRuleException.class, () -> authService.register(request));
	}

	@Test
	void login_whenCredentialsAreValidReturnsBearerToken() {
		LoginRequest request = new LoginRequest("john@example.com", "Secure@123");
		User user = User.builder()
				.id(1L)
				.email("john@example.com")
				.password(passwordEncoder.encode("Secure@123"))
				.role(Role.ROLE_USER)
				.build();

		when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
		when(jwtUtil.generateToken(user)).thenReturn("jwt-token");
		when(jwtUtil.getExpirationSeconds()).thenReturn(86400L);

		LoginResponse response = authService.login(request);

		assertEquals("jwt-token", response.token());
		assertEquals("Bearer", response.tokenType());
		assertEquals(86400L, response.expiresIn());
	}

	@Test
	void login_whenPasswordIsInvalidThrowsBadCredentialsException() {
		LoginRequest request = new LoginRequest("john@example.com", "Wrong@123");
		User user = User.builder()
				.email("john@example.com")
				.password(passwordEncoder.encode("Secure@123"))
				.role(Role.ROLE_USER)
				.build();

		when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

		assertThrows(BadCredentialsException.class, () -> authService.login(request));
	}
}
