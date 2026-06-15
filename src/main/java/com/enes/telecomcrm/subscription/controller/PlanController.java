package com.enes.telecomcrm.subscription.controller;

import java.util.List;

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
import com.enes.telecomcrm.subscription.dto.PlanResponse;
import com.enes.telecomcrm.subscription.service.PlanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

	private final PlanService planService;

	public PlanController(PlanService planService) {
		this.planService = planService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<PlanResponse> createPlan(@Valid @RequestBody PlanRequest request) {
		return ApiResponse.success("Plan created successfully", planService.createPlan(request));
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<PlanResponse>> getAllPlans() {
		return ApiResponse.success("Plans retrieved successfully", planService.getAllPlans());
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<PlanResponse> getPlanById(@PathVariable Long id) {
		return ApiResponse.success("Plan retrieved successfully", planService.getPlanById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<PlanResponse> updatePlan(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
		return ApiResponse.success("Plan updated successfully", planService.updatePlan(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<PlanResponse> deletePlan(@PathVariable Long id) {
		return ApiResponse.success("Plan deleted successfully", planService.deletePlan(id));
	}
}
