package com.enes.telecomcrm.subscription.controller;

import java.util.List;

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
import com.enes.telecomcrm.subscription.dto.SubscriptionResponse;
import com.enes.telecomcrm.subscription.service.SubscriptionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	public SubscriptionController(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SubscriptionResponse> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
		return ApiResponse.success("Subscription created successfully", subscriptionService.createSubscription(request));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<List<SubscriptionResponse>> getAllSubscriptions() {
		return ApiResponse.success("Subscriptions retrieved successfully", subscriptionService.getAllSubscriptions());
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.canAccessSubscription(#id)")
	public ApiResponse<SubscriptionResponse> getSubscriptionById(@PathVariable Long id) {
		return ApiResponse.success("Subscription retrieved successfully", subscriptionService.getSubscriptionById(id));
	}

	@GetMapping("/user/{userId}")
	@PreAuthorize("@authorizationService.canAccessUserSubscriptions(#userId)")
	public ApiResponse<List<SubscriptionResponse>> getSubscriptionsByUserId(@PathVariable Long userId) {
		return ApiResponse.success("User subscriptions retrieved successfully", subscriptionService.getSubscriptionsByUserId(userId));
	}

	@PatchMapping("/{id}/suspend")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SubscriptionResponse> suspendSubscription(@PathVariable Long id) {
		return ApiResponse.success("Subscription suspended successfully", subscriptionService.suspendSubscription(id));
	}

	@PatchMapping("/{id}/cancel")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SubscriptionResponse> cancelSubscription(@PathVariable Long id) {
		return ApiResponse.success("Subscription cancelled successfully", subscriptionService.cancelSubscription(id));
	}
}
