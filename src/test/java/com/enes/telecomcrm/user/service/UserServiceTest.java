package com.enes.telecomcrm.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.search.service.UserSearchIndexService;
import com.enes.telecomcrm.user.dto.UserRequest;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.exception.UserNotFoundException;
import com.enes.telecomcrm.user.mapper.UserMapper;
import com.enes.telecomcrm.user.repository.UserRepository;

class UserServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final UserMapper userMapper = mock(UserMapper.class);
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
	private final UserSearchIndexService userSearchIndexService = mock(UserSearchIndexService.class);
	private final UserService userService = new UserService(
			userRepository,
			userMapper,
			passwordEncoder,
			userSearchIndexService
	);

	@Test
	void getAllUsers_returnsMappedResponses() {
		List<User> users = List.of(user(1L, "john@example.com"), user(2L, "jane@example.com"));
		List<UserResponse> responses = List.of(response(1L, "john@example.com"), response(2L, "jane@example.com"));

		when(userRepository.findAll()).thenReturn(users);
		when(userMapper.toResponseList(users)).thenReturn(responses);

		assertEquals(responses, userService.getAllUsers());
	}

	@Test
	void getUserById_whenUserExistsReturnsMappedResponse() {
		User user = user(1L, "john@example.com");
		UserResponse response = response(1L, "john@example.com");

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userMapper.toResponse(user)).thenReturn(response);

		assertEquals(response, userService.getUserById(1L));
	}

	@Test
	void getUserById_whenUserDoesNotExistThrowsUserNotFoundException() {
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
	}

	@Test
	void updateUser_hashesPasswordNormalizesEmailAndReturnsMappedResponse() {
		User existingUser = user(1L, "old@example.com");
		UserRequest request = new UserRequest("Jane", "Doe", "JANE@example.com", "Secure@123");
		UserResponse response = response(1L, "jane@example.com");

		when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
		when(userRepository.existsByEmailAndIdNot("jane@example.com", 1L)).thenReturn(false);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userMapper.toResponse(any(User.class))).thenReturn(response);

		UserResponse updatedResponse = userService.updateUser(1L, request);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertEquals(response, updatedResponse);
		assertEquals("Jane", savedUser.getFirstName());
		assertEquals("Doe", savedUser.getLastName());
		assertEquals("jane@example.com", savedUser.getEmail());
		assertNotEquals("Secure@123", savedUser.getPassword());
		assertTrue(passwordEncoder.matches("Secure@123", savedUser.getPassword()));
		assertEquals(Role.ROLE_USER, savedUser.getRole());
		verify(userSearchIndexService).index(savedUser);
	}

	@Test
	void updateUser_whenEmailBelongsToAnotherUserThrowsBusinessRuleException() {
		User existingUser = user(1L, "old@example.com");
		UserRequest request = new UserRequest("Jane", "Doe", "jane@example.com", "Secure@123");

		when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
		when(userRepository.existsByEmailAndIdNot("jane@example.com", 1L)).thenReturn(true);

		assertThrows(BusinessRuleException.class, () -> userService.updateUser(1L, request));
	}

	@Test
	void updateUser_whenUserDoesNotExistThrowsUserNotFoundException() {
		UserRequest request = new UserRequest("Jane", "Doe", "jane@example.com", "Secure@123");
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.updateUser(99L, request));
	}

	@Test
	void deleteUser_deletesExistingUserAndReturnsMappedResponse() {
		User user = user(1L, "john@example.com");
		UserResponse response = response(1L, "john@example.com");

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userMapper.toResponse(user)).thenReturn(response);

		assertEquals(response, userService.deleteUser(1L));
		verify(userRepository).delete(user);
		verify(userSearchIndexService).delete(1L);
	}

	@Test
	void deleteUser_whenUserDoesNotExistThrowsUserNotFoundException() {
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));
	}

	private User user(Long id, String email) {
		return User.builder()
				.id(id)
				.firstName("John")
				.lastName("Doe")
				.email(email)
				.password("hashed-password")
				.role(Role.ROLE_USER)
				.build();
	}

	private UserResponse response(Long id, String email) {
		return new UserResponse(id, "John", "Doe", email, "ROLE_USER", null, null);
	}
}
