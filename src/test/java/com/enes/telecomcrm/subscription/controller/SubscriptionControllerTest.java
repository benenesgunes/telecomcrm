package com.enes.telecomcrm.subscription.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.common.exception.GlobalExceptionHandler;
import com.enes.telecomcrm.subscription.dto.PlanResponse;
import com.enes.telecomcrm.subscription.dto.SubscriptionRequest;
import com.enes.telecomcrm.subscription.dto.SubscriptionResponse;
import com.enes.telecomcrm.subscription.exception.SubscriptionNotFoundException;
import com.enes.telecomcrm.subscription.service.SubscriptionService;
import com.enes.telecomcrm.user.dto.UserResponse;

class SubscriptionControllerTest {

	private static final String SUBSCRIPTION_REQUEST = """
			{
			  "userId": 1,
			  "planId": 2,
			  "startDate": "2026-06-15"
			}
			""";

	private final SubscriptionService subscriptionService = mock(SubscriptionService.class);
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new SubscriptionController(subscriptionService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void createSubscription_returnsSubscriptionResponseDto() throws Exception {
		SubscriptionRequest request = new SubscriptionRequest(1L, 2L, LocalDate.of(2026, 6, 15));
		when(subscriptionService.createSubscription(request)).thenReturn(response(10L, "ACTIVE"));

		mockMvc.perform(post("/api/v1/subscriptions")
						.contentType(MediaType.APPLICATION_JSON)
						.content(SUBSCRIPTION_REQUEST))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Subscription created successfully"))
				.andExpect(jsonPath("$.data.id").value(10))
				.andExpect(jsonPath("$.data.status").value("ACTIVE"))
				.andExpect(jsonPath("$.data.user.id").value(1))
				.andExpect(jsonPath("$.data.plan.id").value(2));
	}

	@Test
	void getAllSubscriptions_returnsSubscriptionResponseDtos() throws Exception {
		when(subscriptionService.getAllSubscriptions()).thenReturn(List.of(response(10L, "ACTIVE"), response(11L, "SUSPENDED")));

		mockMvc.perform(get("/api/v1/subscriptions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Subscriptions retrieved successfully"))
				.andExpect(jsonPath("$.data[0].id").value(10))
				.andExpect(jsonPath("$.data[1].status").value("SUSPENDED"));
	}

	@Test
	void getSubscriptionById_returnsSubscriptionResponseDto() throws Exception {
		when(subscriptionService.getSubscriptionById(10L)).thenReturn(response(10L, "ACTIVE"));

		mockMvc.perform(get("/api/v1/subscriptions/10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Subscription retrieved successfully"))
				.andExpect(jsonPath("$.data.id").value(10));
	}

	@Test
	void getSubscriptionsByUserId_returnsSubscriptionResponseDtos() throws Exception {
		when(subscriptionService.getSubscriptionsByUserId(1L)).thenReturn(List.of(response(10L, "ACTIVE")));

		mockMvc.perform(get("/api/v1/subscriptions/user/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User subscriptions retrieved successfully"))
				.andExpect(jsonPath("$.data[0].user.id").value(1));
	}

	@Test
	void suspendSubscription_returnsSuspendedSubscriptionResponseDto() throws Exception {
		when(subscriptionService.suspendSubscription(10L)).thenReturn(response(10L, "SUSPENDED"));

		mockMvc.perform(patch("/api/v1/subscriptions/10/suspend"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Subscription suspended successfully"))
				.andExpect(jsonPath("$.data.status").value("SUSPENDED"));
	}

	@Test
	void cancelSubscription_returnsCancelledSubscriptionResponseDto() throws Exception {
		when(subscriptionService.cancelSubscription(10L)).thenReturn(response(10L, "CANCELLED"));

		mockMvc.perform(patch("/api/v1/subscriptions/10/cancel"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Subscription cancelled successfully"))
				.andExpect(jsonPath("$.data.status").value("CANCELLED"));
	}

	@Test
	void createSubscription_whenRequestIsInvalidReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/v1/subscriptions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void getSubscriptionById_whenMissingReturnsNotFound() throws Exception {
		when(subscriptionService.getSubscriptionById(99L)).thenThrow(new SubscriptionNotFoundException(99L));

		mockMvc.perform(get("/api/v1/subscriptions/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Subscription not found with id: 99"));
	}

	@Test
	void suspendSubscription_whenTransitionInvalidReturnsUnprocessableEntity() throws Exception {
		when(subscriptionService.suspendSubscription(10L)).thenThrow(new BusinessRuleException("Cancelled subscription cannot be modified"));

		mockMvc.perform(patch("/api/v1/subscriptions/10/suspend"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Cancelled subscription cannot be modified"));
	}

	private SubscriptionResponse response(Long id, String status) {
		return new SubscriptionResponse(
				id,
				status,
				LocalDate.of(2026, 6, 15),
				null,
				new UserResponse(1L, "John", "Doe", "john@example.com", "ROLE_USER", null, null),
				new PlanResponse(2L, "Mobile Starter", "MOBILE", new BigDecimal("199.99"), "Basic mobile package")
		);
	}
}
