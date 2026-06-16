package com.enes.telecomcrm.analytics.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enes.telecomcrm.analytics.dto.AdminDashboardResponse;
import com.enes.telecomcrm.analytics.dto.AgentDashboardResponse;
import com.enes.telecomcrm.analytics.dto.SubscriptionDashboardResponse;
import com.enes.telecomcrm.analytics.dto.TicketDashboardResponse;
import com.enes.telecomcrm.analytics.service.DashboardService;
import com.enes.telecomcrm.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboards", description = "Aggregated analytics and dashboard endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get admin dashboard", description = "Returns total users, subscription counts, and ticket counts. Requires ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Admin dashboard retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<AdminDashboardResponse> getAdminDashboard() {
		return ApiResponse.success("Admin dashboard retrieved successfully", dashboardService.getAdminMetrics());
	}

	@GetMapping("/subscriptions")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get subscription dashboard", description = "Returns popular plans, monthly growth, and status distribution. Requires ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription dashboard retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<SubscriptionDashboardResponse> getSubscriptionDashboard() {
		return ApiResponse.success("Subscription dashboard retrieved successfully", dashboardService.getSubscriptionMetrics());
	}

	@GetMapping("/tickets")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get ticket dashboard", description = "Returns ticket status and priority distribution. Requires ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket dashboard retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<TicketDashboardResponse> getTicketDashboard() {
		return ApiResponse.success("Ticket dashboard retrieved successfully", dashboardService.getTicketMetrics());
	}

	@GetMapping("/agent")
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	@Operation(summary = "Get agent dashboard", description = "Returns assigned tickets, resolved tickets, and average resolution time for the authenticated agent.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Agent dashboard retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<AgentDashboardResponse> getAgentDashboard() {
		return ApiResponse.success("Agent dashboard retrieved successfully", dashboardService.getAgentMetrics());
	}
}
