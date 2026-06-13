package com.enes.telecomcrm.auth.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.enes.telecomcrm.auth.dto.LoginRequest;
import com.enes.telecomcrm.auth.dto.LoginResponse;
import com.enes.telecomcrm.auth.dto.RegisterRequest;
import com.enes.telecomcrm.auth.service.AuthService;
import com.enes.telecomcrm.common.exception.GlobalExceptionHandler;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

class AuthControllerTest {

	private final AuthService authService = mock(AuthService.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void register_returnsCreatedUserResponse() throws Exception {
		RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "Secure@123");
		UserResponse response = new UserResponse(1L, "John", "Doe", "john@example.com", "ROLE_USER", null, null);

		when(authService.register(request)).thenReturn(response);

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User registered successfully"))
				.andExpect(jsonPath("$.data.email").value("john@example.com"))
				.andExpect(jsonPath("$.data.role").value("ROLE_USER"));
	}

	@Test
	void login_returnsJwtLoginResponse() throws Exception {
		LoginRequest request = new LoginRequest("john@example.com", "Secure@123");
		LoginResponse response = new LoginResponse("jwt-token", "Bearer", 86400L);

		when(authService.login(request)).thenReturn(response);

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Login successful"))
				.andExpect(jsonPath("$.data.token").value("jwt-token"))
				.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.data.expiresIn").value(86400));
	}
}
