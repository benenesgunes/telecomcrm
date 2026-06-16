package com.enes.telecomcrm.auth.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.enes.telecomcrm.analytics.service.DashboardService;
import com.enes.telecomcrm.search.service.SearchService;
import com.enes.telecomcrm.subscription.service.PlanService;
import com.enes.telecomcrm.subscription.service.SubscriptionService;
import com.enes.telecomcrm.ticket.repository.TicketRepository;
import com.enes.telecomcrm.ticket.service.TicketCommentService;
import com.enes.telecomcrm.ticket.service.TicketService;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationTest {

	private static final String PLAN_REQUEST = """
			{
			  "name": "Mobile Starter",
			  "type": "MOBILE",
			  "monthlyPrice": 199.99,
			  "description": "Basic mobile package"
			}
			""";
	private static final String SUBSCRIPTION_REQUEST = """
			{
			  "userId": 1,
			  "planId": 1,
			  "startDate": "2026-06-13"
			}
			""";
	private static final String TICKET_REQUEST = """
			{
			  "title": "Internet drops",
			  "description": "Connection drops every morning.",
			  "priority": "HIGH"
			}
			""";
	private static final String TICKET_ASSIGN_REQUEST = """
			{
			  "agentId": 2
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private PlanService planService;

	@MockitoBean
	private SubscriptionService subscriptionService;

	@MockitoBean
	private TicketService ticketService;

	@MockitoBean
	private TicketCommentService ticketCommentService;

	@MockitoBean
	private TicketRepository ticketRepository;

	@MockitoBean
	private DashboardService dashboardService;

	@MockitoBean
	private SearchService searchService;

	@Test
	void protectedEndpointWithoutAuthenticationReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/users"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Authentication required"));
	}

	@Test
	@WithMockUser(roles = "USER")
	void userCannotListUsers() throws Exception {
		mockMvc.perform(get("/api/v1/users"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Insufficient permissions"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCanListUsers() throws Exception {
		mockMvc.perform(get("/api/v1/users"))
				.andExpect(status().isOk());
	}

	@Test
	@WithTelecomUser(id = 1, role = Role.ROLE_USER)
	void userCanAccessOwnProfileButNotAnotherProfile() throws Exception {
		mockMvc.perform(get("/api/v1/users/1"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/users/2"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "USER")
	void authenticatedUsersCanReadPlansButOnlyAdminCanCreatePlans() throws Exception {
		mockMvc.perform(get("/api/v1/plans"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/plans")
						.contentType(MediaType.APPLICATION_JSON)
						.content(PLAN_REQUEST))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCanCreatePlans() throws Exception {
		mockMvc.perform(post("/api/v1/plans")
						.contentType(MediaType.APPLICATION_JSON)
						.content(PLAN_REQUEST))
				.andExpect(status().isOk());
	}

	@Test
	@WithTelecomUser(id = 1, role = Role.ROLE_USER)
	void userCanReadOwnSubscriptionsButCannotCreateOrListAllSubscriptions() throws Exception {
		mockMvc.perform(get("/api/v1/subscriptions/user/1"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/subscriptions/user/2"))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/subscriptions"))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/v1/subscriptions")
						.contentType(MediaType.APPLICATION_JSON)
						.content(SUBSCRIPTION_REQUEST))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCanManageSubscriptions() throws Exception {
		mockMvc.perform(get("/api/v1/subscriptions"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/subscriptions")
						.contentType(MediaType.APPLICATION_JSON)
						.content(SUBSCRIPTION_REQUEST))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/v1/subscriptions/1/suspend"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "USER")
	void userCanCreateAndReadOwnTicketsButCannotListAllOrAssignTickets() throws Exception {
		mockMvc.perform(post("/api/v1/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(TICKET_REQUEST))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/tickets/my"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/tickets"))
				.andExpect(status().isForbidden());

		mockMvc.perform(patch("/api/v1/tickets/1/assign")
						.contentType(MediaType.APPLICATION_JSON)
						.content(TICKET_ASSIGN_REQUEST))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "SUPPORT_AGENT")
	void supportAgentCanListAssignAndResolveTicketsButCannotCloseOrCreateTickets() throws Exception {
		mockMvc.perform(get("/api/v1/tickets"))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/v1/tickets/1/assign")
						.contentType(MediaType.APPLICATION_JSON)
						.content(TICKET_ASSIGN_REQUEST))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/v1/tickets/1/resolve"))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/v1/tickets/1/close"))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/v1/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(TICKET_REQUEST))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCanCloseTicketsAndViewDashboards() throws Exception {
		mockMvc.perform(patch("/api/v1/tickets/1/close"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/dashboard/admin"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/dashboard/subscriptions"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "USER")
	void userCannotViewAdminDashboard() throws Exception {
		mockMvc.perform(get("/api/v1/dashboard/admin"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "USER")
	void userCannotSearchTicketsOrUsers() throws Exception {
		mockMvc.perform(get("/api/v1/search/tickets").param("q", "internet"))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/search/users").param("q", "john"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "SUPPORT_AGENT")
	void supportAgentCanSearchTicketsButCannotSearchUsers() throws Exception {
		mockMvc.perform(get("/api/v1/search/tickets").param("q", "internet"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/search/users").param("q", "john"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCanSearchTicketsAndUsers() throws Exception {
		mockMvc.perform(get("/api/v1/search/tickets").param("q", "internet"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/search/users").param("q", "john"))
				.andExpect(status().isOk());
	}

	@Test
	@WithTelecomUser(id = 2, role = Role.ROLE_SUPPORT_AGENT)
	void supportAgentCanViewAgentDashboardAndAddComments() throws Exception {
		org.mockito.Mockito.when(ticketRepository.existsByIdAndAssignedAgentId(1L, 2L)).thenReturn(true);

		mockMvc.perform(get("/api/v1/dashboard/agent"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/tickets/1/comments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"Looking into this.\"}"))
				.andExpect(status().isOk());
	}

	@Test
	@WithTelecomUser(id = 2, role = Role.ROLE_SUPPORT_AGENT)
	void supportAgentCannotCommentOnUnassignedTicket() throws Exception {
		org.mockito.Mockito.when(ticketRepository.existsByIdAndAssignedAgentId(1L, 2L)).thenReturn(false);

		mockMvc.perform(post("/api/v1/tickets/1/comments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"Looking into this.\"}"))
				.andExpect(status().isForbidden());
	}
}
