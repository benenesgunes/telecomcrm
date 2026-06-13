package com.enes.telecomcrm.subscription.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enes.telecomcrm.common.dto.ApiResponse;
import com.enes.telecomcrm.subscription.dto.PlanRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> createPlan(@Valid @RequestBody PlanRequest request) {
		return ApiResponse.success("Plan created successfully", null);
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<Void> getAllPlans() {
		return ApiResponse.success("Plans retrieved successfully", null);
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<Void> getPlanById(@PathVariable Long id) {
		return ApiResponse.success("Plan retrieved successfully", null);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> updatePlan(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
		return ApiResponse.success("Plan updated successfully", null);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> deletePlan(@PathVariable Long id) {
		return ApiResponse.success("Plan deleted successfully", null);
	}
}
