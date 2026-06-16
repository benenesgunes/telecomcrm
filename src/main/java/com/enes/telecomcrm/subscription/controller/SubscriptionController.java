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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "Customer subscription lifecycle endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	public SubscriptionController(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create subscription", description = "Creates a subscription for a user and plan. Requires ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription created successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body or business rule violation")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<SubscriptionResponse> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
		return ApiResponse.success("Subscription created successfully", subscriptionService.createSubscription(request));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "List subscriptions", description = "Returns all subscriptions. Requires ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscriptions retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<List<SubscriptionResponse>> getAllSubscriptions() {
		return ApiResponse.success("Subscriptions retrieved successfully", subscriptionService.getAllSubscriptions());
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.canAccessSubscription(#id)")
	@Operation(summary = "Get subscription", description = "Returns a subscription by id for the owner or admins.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Subscription not found")
	public ApiResponse<SubscriptionResponse> getSubscriptionById(@PathVariable Long id) {
		return ApiResponse.success("Subscription retrieved successfully", subscriptionService.getSubscriptionById(id));
	}

	@GetMapping("/user/{userId}")
	@PreAuthorize("@authorizationService.canAccessUserSubscriptions(#userId)")
	@Operation(summary = "List user subscriptions", description = "Returns subscriptions for a user. Users can access their own; admins can access all.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User subscriptions retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<List<SubscriptionResponse>> getSubscriptionsByUserId(@PathVariable Long userId) {
		return ApiResponse.success("User subscriptions retrieved successfully", subscriptionService.getSubscriptionsByUserId(userId));
	}

	@PatchMapping("/{id}/suspend")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Suspend subscription", description = "Suspends an active subscription. Requires ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription suspended successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid subscription transition")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Subscription not found")
	public ApiResponse<SubscriptionResponse> suspendSubscription(@PathVariable Long id) {
		return ApiResponse.success("Subscription suspended successfully", subscriptionService.suspendSubscription(id));
	}

	@PatchMapping("/{id}/cancel")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Cancel subscription", description = "Cancels a subscription. Requires ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription cancelled successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid subscription transition")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Subscription not found")
	public ApiResponse<SubscriptionResponse> cancelSubscription(@PathVariable Long id) {
		return ApiResponse.success("Subscription cancelled successfully", subscriptionService.cancelSubscription(id));
	}
}
