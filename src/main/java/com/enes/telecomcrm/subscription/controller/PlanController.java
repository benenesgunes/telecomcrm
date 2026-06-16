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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = "Plans", description = "Telecom plan catalog endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PlanController {

	private final PlanService planService;

	public PlanController(PlanService planService) {
		this.planService = planService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create plan", description = "Creates a telecom plan. Requires ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plan created successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<PlanResponse> createPlan(@Valid @RequestBody PlanRequest request) {
		return ApiResponse.success("Plan created successfully", planService.createPlan(request));
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "List plans", description = "Returns all telecom plans for authenticated users.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plans retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
	public ApiResponse<List<PlanResponse>> getAllPlans() {
		return ApiResponse.success("Plans retrieved successfully", planService.getAllPlans());
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get plan", description = "Returns a plan by id for authenticated users.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plan retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan not found")
	public ApiResponse<PlanResponse> getPlanById(@PathVariable Long id) {
		return ApiResponse.success("Plan retrieved successfully", planService.getPlanById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Update plan", description = "Updates a telecom plan. Requires ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plan updated successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan not found")
	public ApiResponse<PlanResponse> updatePlan(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
		return ApiResponse.success("Plan updated successfully", planService.updatePlan(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete plan", description = "Deletes a telecom plan. Requires ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plan deleted successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan not found")
	public ApiResponse<PlanResponse> deletePlan(@PathVariable Long id) {
		return ApiResponse.success("Plan deleted successfully", planService.deletePlan(id));
	}
}
