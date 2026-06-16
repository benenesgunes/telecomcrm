package com.enes.telecomcrm.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.enes.telecomcrm.analytics.dto.AdminDashboardResponse;
import com.enes.telecomcrm.analytics.dto.AgentDashboardResponse;
import com.enes.telecomcrm.analytics.dto.SubscriptionDashboardResponse;
import com.enes.telecomcrm.analytics.dto.TicketDashboardResponse;
import com.enes.telecomcrm.auth.security.UserPrincipal;
import com.enes.telecomcrm.common.exception.UnauthorizedException;
import com.enes.telecomcrm.subscription.entity.SubscriptionStatus;
import com.enes.telecomcrm.subscription.repository.PlanRepository;
import com.enes.telecomcrm.subscription.repository.SubscriptionRepository;
import com.enes.telecomcrm.ticket.entity.TicketPriority;
import com.enes.telecomcrm.ticket.entity.TicketStatus;
import com.enes.telecomcrm.ticket.repository.TicketRepository;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.exception.UserNotFoundException;
import com.enes.telecomcrm.user.repository.UserRepository;

class DashboardServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
	private final PlanRepository planRepository = mock(PlanRepository.class);
	private final TicketRepository ticketRepository = mock(TicketRepository.class);
	private final DashboardService dashboardService = new DashboardService(
			userRepository,
			subscriptionRepository,
			planRepository,
			ticketRepository
	);

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getAdminMetrics_returnsAggregatedCounts() {
		when(userRepository.count()).thenReturn(42L);
		when(subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE)).thenReturn(18L);
		when(subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED)).thenReturn(4L);
		when(ticketRepository.countByStatus(TicketStatus.OPEN)).thenReturn(7L);
		when(ticketRepository.countByStatus(TicketStatus.RESOLVED)).thenReturn(11L);

		AdminDashboardResponse response = dashboardService.getAdminMetrics();

		assertEquals(42L, response.totalUsers());
		assertEquals(18L, response.activeSubscriptions());
		assertEquals(4L, response.cancelledSubscriptions());
		assertEquals(7L, response.openTickets());
		assertEquals(11L, response.resolvedTickets());
	}

	@Test
	void getSubscriptionMetrics_returnsPopularPlansMonthlyGrowthAndStatusDistribution() {
		YearMonth currentMonth = YearMonth.now();
		YearMonth previousMonth = currentMonth.minusMonths(1);

		when(planRepository.findPopularPlans()).thenReturn(List.of(
				new PlanPopularityViewStub(1L, "Home Internet", 15L),
				new PlanPopularityViewStub(2L, "Mobile Starter", 8L)
		));
		when(subscriptionRepository.countMonthlyGrowthSince(currentMonth.minusMonths(11).atDay(1))).thenReturn(List.of(
				new MonthlyGrowthViewStub(previousMonth.getYear(), previousMonth.getMonthValue(), 3L),
				new MonthlyGrowthViewStub(currentMonth.getYear(), currentMonth.getMonthValue(), 5L)
		));
		when(subscriptionRepository.countByStatusDistribution()).thenReturn(List.of(
				new StatusCountViewStub(SubscriptionStatus.ACTIVE, 15L),
				new StatusCountViewStub(SubscriptionStatus.CANCELLED, 2L)
		));

		SubscriptionDashboardResponse response = dashboardService.getSubscriptionMetrics();

		assertEquals(2, response.mostPopularPlans().size());
		assertEquals(1L, response.mostPopularPlans().getFirst().planId());
		assertEquals("Home Internet", response.mostPopularPlans().getFirst().planName());
		assertEquals(15L, response.mostPopularPlans().getFirst().activeSubscriberCount());

		assertEquals(12, response.monthlyGrowth().size());
		assertEquals(3L, response.monthlyGrowth().get(previousMonth.toString()));
		assertEquals(5L, response.monthlyGrowth().get(currentMonth.toString()));
		assertEquals(0L, response.monthlyGrowth().get(currentMonth.minusMonths(11).toString()));

		assertEquals(Map.of(
				"ACTIVE", 15L,
				"SUSPENDED", 0L,
				"CANCELLED", 2L,
				"EXPIRED", 0L
		), response.statusDistribution());
	}

	@Test
	void getTicketMetrics_returnsStatusAndPriorityDistribution() {
		when(ticketRepository.countByStatusDistribution()).thenReturn(List.of(
				new TicketStatusCountViewStub(TicketStatus.OPEN, 7L),
				new TicketStatusCountViewStub(TicketStatus.RESOLVED, 11L)
		));
		when(ticketRepository.countByPriorityDistribution()).thenReturn(List.of(
				new TicketPriorityCountViewStub(TicketPriority.HIGH, 4L),
				new TicketPriorityCountViewStub(TicketPriority.MEDIUM, 9L)
		));

		TicketDashboardResponse response = dashboardService.getTicketMetrics();

		assertEquals(Map.of(
				"OPEN", 7L,
				"IN_PROGRESS", 0L,
				"RESOLVED", 11L,
				"CLOSED", 0L
		), response.statusDistribution());
		assertEquals(Map.of(
				"LOW", 0L,
				"MEDIUM", 9L,
				"HIGH", 4L
		), response.priorityDistribution());
	}

	@Test
	void getAgentMetrics_returnsCurrentAgentMetrics() {
		authenticate(2L, Role.ROLE_SUPPORT_AGENT);
		when(userRepository.findById(2L)).thenReturn(Optional.of(User.builder()
				.id(2L)
				.firstName("Support")
				.lastName("Agent")
				.email("agent@example.com")
				.password("hashed-password")
				.role(Role.ROLE_SUPPORT_AGENT)
				.build()));
		when(ticketRepository.countByAssignedAgentId(2L)).thenReturn(12L);
		when(ticketRepository.countByAssignedAgentIdAndStatus(2L, TicketStatus.RESOLVED)).thenReturn(9L);
		when(ticketRepository.averageResolutionTimeHoursByAssignedAgentId(2L)).thenReturn(4.5);

		AgentDashboardResponse response = dashboardService.getAgentMetrics();

		assertEquals(2L, response.agentId());
		assertEquals("Support Agent", response.agentName());
		assertEquals(12L, response.assignedTickets());
		assertEquals(9L, response.resolvedTickets());
		assertEquals(4.5, response.averageResolutionTimeHours());
	}

	@Test
	void getAgentMetrics_whenCurrentUserMissingThrowsUserNotFoundException() {
		authenticate(2L, Role.ROLE_SUPPORT_AGENT);
		when(userRepository.findById(2L)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, dashboardService::getAgentMetrics);
	}

	@Test
	void getAgentMetrics_whenUserIsNotAuthenticatedThrowsUnauthorizedException() {
		assertThrows(UnauthorizedException.class, dashboardService::getAgentMetrics);
	}

	private void authenticate(Long id, Role role) {
		UserPrincipal principal = new UserPrincipal(id, "user%d@example.com".formatted(id), "password", role);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				principal,
				null,
				principal.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private record PlanPopularityViewStub(
			Long planId,
			String planName,
			long activeSubscriberCount
	) implements PlanRepository.PlanPopularityView {

		@Override
		public Long getPlanId() {
			return planId;
		}

		@Override
		public String getPlanName() {
			return planName;
		}

		@Override
		public long getActiveSubscriberCount() {
			return activeSubscriberCount;
		}
	}

	private record MonthlyGrowthViewStub(
			Integer year,
			Integer month,
			long subscriptionCount
	) implements SubscriptionRepository.MonthlySubscriptionGrowthView {

		@Override
		public Integer getYear() {
			return year;
		}

		@Override
		public Integer getMonth() {
			return month;
		}

		@Override
		public long getSubscriptionCount() {
			return subscriptionCount;
		}
	}

	private record StatusCountViewStub(
			SubscriptionStatus status,
			long subscriptionCount
	) implements SubscriptionRepository.SubscriptionStatusCountView {

		@Override
		public SubscriptionStatus getStatus() {
			return status;
		}

		@Override
		public long getSubscriptionCount() {
			return subscriptionCount;
		}
	}

	private record TicketStatusCountViewStub(
			TicketStatus status,
			long ticketCount
	) implements TicketRepository.TicketStatusCountView {

		@Override
		public TicketStatus getStatus() {
			return status;
		}

		@Override
		public long getTicketCount() {
			return ticketCount;
		}
	}

	private record TicketPriorityCountViewStub(
			TicketPriority priority,
			long ticketCount
	) implements TicketRepository.TicketPriorityCountView {

		@Override
		public TicketPriority getPriority() {
			return priority;
		}

		@Override
		public long getTicketCount() {
			return ticketCount;
		}
	}
}
