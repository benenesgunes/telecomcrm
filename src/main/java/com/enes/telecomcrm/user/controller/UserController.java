package com.enes.telecomcrm.user.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enes.telecomcrm.common.dto.ApiResponse;
import com.enes.telecomcrm.user.dto.UserRequest;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.enes.telecomcrm.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<List<UserResponse>> getAllUsers() {
		return ApiResponse.success("Users retrieved successfully", userService.getAllUsers());
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.canAccessUser(#id)")
	public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
		return ApiResponse.success("User retrieved successfully", userService.getUserById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.canAccessUser(#id)")
	public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
		return ApiResponse.success("User updated successfully", userService.updateUser(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UserResponse> deleteUser(@PathVariable Long id) {
		return ApiResponse.success("User deleted successfully", userService.deleteUser(id));
	}
}
