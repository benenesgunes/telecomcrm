package com.enes.telecomcrm.subscription.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enes.telecomcrm.common.dto.ApiResponse;
import com.enes.telecomcrm.subscription.dto.SubscriptionRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
		return ApiResponse.success("Subscription created successfully", null);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> getAllSubscriptions() {
		return ApiResponse.success("Subscriptions retrieved successfully", null);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.canAccessSubscription(#id)")
	public ApiResponse<Void> getSubscriptionById(@PathVariable Long id) {
		return ApiResponse.success("Subscription retrieved successfully", null);
	}

	@GetMapping("/user/{userId}")
	@PreAuthorize("@authorizationService.canAccessUserSubscriptions(#userId)")
	public ApiResponse<Void> getSubscriptionsByUserId(@PathVariable Long userId) {
		return ApiResponse.success("User subscriptions retrieved successfully", null);
	}

	@PatchMapping("/{id}/suspend")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> suspendSubscription(@PathVariable Long id) {
		return ApiResponse.success("Subscription suspended successfully", null);
	}

	@PatchMapping("/{id}/cancel")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> cancelSubscription(@PathVariable Long id) {
		return ApiResponse.success("Subscription cancelled successfully", null);
	}
}
