package com.enes.telecomcrm.user.controller;

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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> getAllUsers() {
		return ApiResponse.success("Users retrieved successfully", null);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.canAccessUser(#id)")
	public ApiResponse<Void> getUserById(@PathVariable Long id) {
		return ApiResponse.success("User retrieved successfully", null);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.canAccessUser(#id)")
	public ApiResponse<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
		return ApiResponse.success("User updated successfully", null);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> deleteUser(@PathVariable Long id) {
		return ApiResponse.success("User deleted successfully", null);
	}
}
