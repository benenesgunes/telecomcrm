package com.enes.telecomcrm.analytics.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enes.telecomcrm.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> getAdminDashboard() {
		return ApiResponse.success("Admin dashboard retrieved successfully", null);
	}

	@GetMapping("/subscriptions")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> getSubscriptionDashboard() {
		return ApiResponse.success("Subscription dashboard retrieved successfully", null);
	}

	@GetMapping("/tickets")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> getTicketDashboard() {
		return ApiResponse.success("Ticket dashboard retrieved successfully", null);
	}

	@GetMapping("/agent")
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	public ApiResponse<Void> getAgentDashboard() {
		return ApiResponse.success("Agent dashboard retrieved successfully", null);
	}
}
