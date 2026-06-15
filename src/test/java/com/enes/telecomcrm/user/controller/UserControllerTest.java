package com.enes.telecomcrm.user.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.enes.telecomcrm.common.exception.GlobalExceptionHandler;
import com.enes.telecomcrm.user.dto.UserRequest;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.enes.telecomcrm.user.exception.UserNotFoundException;
import com.enes.telecomcrm.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

class UserControllerTest {

	private final UserService userService = mock(UserService.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void getAllUsers_returnsUserResponseDtos() throws Exception {
		when(userService.getAllUsers()).thenReturn(List.of(response(1L), response(2L)));

		mockMvc.perform(get("/api/v1/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Users retrieved successfully"))
				.andExpect(jsonPath("$.data[0].id").value(1))
				.andExpect(jsonPath("$.data[0].email").value("john1@example.com"))
				.andExpect(jsonPath("$.data[1].id").value(2));
	}

	@Test
	void getUserById_returnsUserResponseDto() throws Exception {
		when(userService.getUserById(1L)).thenReturn(response(1L));

		mockMvc.perform(get("/api/v1/users/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User retrieved successfully"))
				.andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.role").value("ROLE_USER"));
	}

	@Test
	void updateUser_returnsUpdatedUserResponseDto() throws Exception {
		UserRequest request = new UserRequest("Jane", "Doe", "jane@example.com", "Secure@123");
		UserResponse response = new UserResponse(1L, "Jane", "Doe", "jane@example.com", "ROLE_USER", null, null);

		when(userService.updateUser(1L, request)).thenReturn(response);

		mockMvc.perform(put("/api/v1/users/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User updated successfully"))
				.andExpect(jsonPath("$.data.email").value("jane@example.com"));
	}

	@Test
	void updateUser_whenRequestIsInvalidReturnsBadRequest() throws Exception {
		String invalidRequest = """
				{
				  "firstName": "J",
				  "lastName": "",
				  "email": "not-an-email",
				  "password": "weak"
				}
				""";

		mockMvc.perform(put("/api/v1/users/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidRequest))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void deleteUser_returnsDeletedUserResponseDto() throws Exception {
		when(userService.deleteUser(1L)).thenReturn(response(1L));

		mockMvc.perform(delete("/api/v1/users/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User deleted successfully"))
				.andExpect(jsonPath("$.data.id").value(1));
	}

	@Test
	void getUserById_whenUserDoesNotExistReturnsNotFound() throws Exception {
		when(userService.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

		mockMvc.perform(get("/api/v1/users/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("User not found with id: 99"));
	}

	private UserResponse response(Long id) {
		return new UserResponse(id, "John", "Doe", "john%d@example.com".formatted(id), "ROLE_USER", null, null);
	}
}
