package com.enes.telecomcrm.analytics.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.enes.telecomcrm.analytics.dto.AdminDashboardResponse;
import com.enes.telecomcrm.analytics.dto.AgentDashboardResponse;
import com.enes.telecomcrm.analytics.dto.PlanPopularityDTO;
import com.enes.telecomcrm.analytics.dto.SubscriptionDashboardResponse;
import com.enes.telecomcrm.analytics.dto.TicketDashboardResponse;
import com.enes.telecomcrm.analytics.service.DashboardService;
import com.enes.telecomcrm.common.exception.GlobalExceptionHandler;

class DashboardControllerTest {

	private final DashboardService dashboardService = mock(DashboardService.class);
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void getAdminDashboard_returnsAdminMetrics() throws Exception {
		when(dashboardService.getAdminMetrics()).thenReturn(new AdminDashboardResponse(42L, 18L, 4L, 7L, 11L));

		mockMvc.perform(get("/api/v1/dashboard/admin"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Admin dashboard retrieved successfully"))
				.andExpect(jsonPath("$.data.totalUsers").value(42))
				.andExpect(jsonPath("$.data.activeSubscriptions").value(18))
				.andExpect(jsonPath("$.data.cancelledSubscriptions").value(4))
				.andExpect(jsonPath("$.data.openTickets").value(7))
				.andExpect(jsonPath("$.data.resolvedTickets").value(11));
	}

	@Test
	void getSubscriptionDashboard_returnsSubscriptionMetrics() throws Exception {
		Map<String, Long> monthlyGrowth = new LinkedHashMap<>();
		monthlyGrowth.put("2026-05", 3L);
		monthlyGrowth.put("2026-06", 5L);
		Map<String, Long> statusDistribution = Map.of("ACTIVE", 15L, "CANCELLED", 2L);

		when(dashboardService.getSubscriptionMetrics()).thenReturn(new SubscriptionDashboardResponse(
				List.of(new PlanPopularityDTO(1L, "Home Internet", 15L)),
				monthlyGrowth,
				statusDistribution
		));

		mockMvc.perform(get("/api/v1/dashboard/subscriptions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Subscription dashboard retrieved successfully"))
				.andExpect(jsonPath("$.data.mostPopularPlans[0].planId").value(1))
				.andExpect(jsonPath("$.data.mostPopularPlans[0].activeSubscriberCount").value(15))
				.andExpect(jsonPath("$['data']['monthlyGrowth']['2026-06']").value(5))
				.andExpect(jsonPath("$.data.statusDistribution.ACTIVE").value(15));
	}

	@Test
	void getTicketDashboard_returnsTicketMetrics() throws Exception {
		when(dashboardService.getTicketMetrics()).thenReturn(new TicketDashboardResponse(
				Map.of("OPEN", 7L, "RESOLVED", 11L),
				Map.of("HIGH", 4L, "MEDIUM", 9L)
		));

		mockMvc.perform(get("/api/v1/dashboard/tickets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Ticket dashboard retrieved successfully"))
				.andExpect(jsonPath("$.data.statusDistribution.OPEN").value(7))
				.andExpect(jsonPath("$.data.statusDistribution.RESOLVED").value(11))
				.andExpect(jsonPath("$.data.priorityDistribution.HIGH").value(4));
	}

	@Test
	void getAgentDashboard_returnsAgentMetrics() throws Exception {
		when(dashboardService.getAgentMetrics()).thenReturn(new AgentDashboardResponse(2L, "Support Agent", 12L, 9L, 4.5));

		mockMvc.perform(get("/api/v1/dashboard/agent"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Agent dashboard retrieved successfully"))
				.andExpect(jsonPath("$.data.agentId").value(2))
				.andExpect(jsonPath("$.data.agentName").value("Support Agent"))
				.andExpect(jsonPath("$.data.assignedTickets").value(12))
				.andExpect(jsonPath("$.data.resolvedTickets").value(9))
				.andExpect(jsonPath("$.data.averageResolutionTimeHours").value(4.5));
	}
}
