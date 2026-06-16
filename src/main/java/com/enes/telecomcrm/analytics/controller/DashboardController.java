package com.enes.telecomcrm.analytics.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enes.telecomcrm.analytics.dto.AdminDashboardResponse;
import com.enes.telecomcrm.analytics.dto.AgentDashboardResponse;
import com.enes.telecomcrm.analytics.dto.SubscriptionDashboardResponse;
import com.enes.telecomcrm.analytics.service.DashboardService;
import com.enes.telecomcrm.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminDashboardResponse> getAdminDashboard() {
		return ApiResponse.success("Admin dashboard retrieved successfully", dashboardService.getAdminMetrics());
	}

	@GetMapping("/subscriptions")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SubscriptionDashboardResponse> getSubscriptionDashboard() {
		return ApiResponse.success("Subscription dashboard retrieved successfully", dashboardService.getSubscriptionMetrics());
	}

	@GetMapping("/tickets")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> getTicketDashboard() {
		return ApiResponse.success("Ticket dashboard retrieved successfully", null);
	}

	@GetMapping("/agent")
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	public ApiResponse<AgentDashboardResponse> getAgentDashboard() {
		return ApiResponse.success("Agent dashboard retrieved successfully", dashboardService.getAgentMetrics());
	}
}
