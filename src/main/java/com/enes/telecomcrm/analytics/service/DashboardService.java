package com.enes.telecomcrm.analytics.service;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enes.telecomcrm.analytics.dto.AdminDashboardResponse;
import com.enes.telecomcrm.analytics.dto.AgentDashboardResponse;
import com.enes.telecomcrm.analytics.dto.PlanPopularityDTO;
import com.enes.telecomcrm.analytics.dto.SubscriptionDashboardResponse;
import com.enes.telecomcrm.analytics.dto.TicketDashboardResponse;
import com.enes.telecomcrm.common.exception.UnauthorizedException;
import com.enes.telecomcrm.common.util.SecurityUtils;
import com.enes.telecomcrm.subscription.entity.SubscriptionStatus;
import com.enes.telecomcrm.subscription.repository.PlanRepository;
import com.enes.telecomcrm.subscription.repository.SubscriptionRepository;
import com.enes.telecomcrm.ticket.entity.TicketPriority;
import com.enes.telecomcrm.ticket.entity.TicketStatus;
import com.enes.telecomcrm.ticket.repository.TicketRepository;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.exception.UserNotFoundException;
import com.enes.telecomcrm.user.repository.UserRepository;

@Service
public class DashboardService {

	private static final int MONTHLY_GROWTH_MONTHS = 12;

	private final UserRepository userRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final PlanRepository planRepository;
	private final TicketRepository ticketRepository;

	public DashboardService(
			UserRepository userRepository,
			SubscriptionRepository subscriptionRepository,
			PlanRepository planRepository,
			TicketRepository ticketRepository
	) {
		this.userRepository = userRepository;
		this.subscriptionRepository = subscriptionRepository;
		this.planRepository = planRepository;
		this.ticketRepository = ticketRepository;
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "dashboard:admin", unless = "#result == null")
	public AdminDashboardResponse getAdminMetrics() {
		return new AdminDashboardResponse(
				userRepository.count(),
				subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE),
				subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED),
				ticketRepository.countByStatus(TicketStatus.OPEN),
				ticketRepository.countByStatus(TicketStatus.RESOLVED)
		);
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "dashboard:subscriptions", unless = "#result == null")
	public SubscriptionDashboardResponse getSubscriptionMetrics() {
		return new SubscriptionDashboardResponse(
				mostPopularPlans(),
				monthlyGrowth(),
				statusDistribution()
		);
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "dashboard:tickets", unless = "#result == null")
	public TicketDashboardResponse getTicketMetrics() {
		return new TicketDashboardResponse(
				ticketStatusDistribution(),
				ticketPriorityDistribution()
		);
	}

	@Transactional(readOnly = true)
	@Cacheable(
			cacheNames = "dashboard:agent",
			key = "T(com.enes.telecomcrm.common.util.SecurityUtils).getCurrentUserId().orElse(null)",
			unless = "#result == null"
	)
	public AgentDashboardResponse getAgentMetrics() {
		Long agentId = SecurityUtils.getCurrentUserId()
				.orElseThrow(() -> new UnauthorizedException("Authenticated user is required"));
		User agent = userRepository.findById(agentId)
				.orElseThrow(() -> new UserNotFoundException(agentId));

		return new AgentDashboardResponse(
				agent.getId(),
				agent.getFirstName() + " " + agent.getLastName(),
				ticketRepository.countByAssignedAgentId(agentId),
				ticketRepository.countByAssignedAgentIdAndStatus(agentId, TicketStatus.RESOLVED),
				ticketRepository.averageResolutionTimeHoursByAssignedAgentId(agentId)
		);
	}

	private List<PlanPopularityDTO> mostPopularPlans() {
		return planRepository.findPopularPlans()
				.stream()
				.map(plan -> new PlanPopularityDTO(
						plan.getPlanId(),
						plan.getPlanName(),
						plan.getActiveSubscriberCount()
				))
				.toList();
	}

	private Map<String, Long> monthlyGrowth() {
		YearMonth firstMonth = YearMonth.now().minusMonths(MONTHLY_GROWTH_MONTHS - 1L);
		Map<String, Long> growth = new LinkedHashMap<>();
		for (int i = 0; i < MONTHLY_GROWTH_MONTHS; i++) {
			growth.put(firstMonth.plusMonths(i).toString(), 0L);
		}

		subscriptionRepository.countMonthlyGrowthSince(firstMonth.atDay(1))
				.forEach(month -> growth.put(
						YearMonth.of(month.getYear(), month.getMonth()).toString(),
						month.getSubscriptionCount()
				));

		return growth;
	}

	private Map<String, Long> statusDistribution() {
		Map<String, Long> distribution = new LinkedHashMap<>();
		Arrays.stream(SubscriptionStatus.values())
				.forEach(status -> distribution.put(status.name(), 0L));

		subscriptionRepository.countByStatusDistribution()
				.forEach(status -> distribution.put(
						status.getStatus().name(),
						status.getSubscriptionCount()
				));

		return distribution;
	}

	private Map<String, Long> ticketStatusDistribution() {
		Map<String, Long> distribution = new LinkedHashMap<>();
		Arrays.stream(TicketStatus.values())
				.forEach(status -> distribution.put(status.name(), 0L));

		ticketRepository.countByStatusDistribution()
				.forEach(status -> distribution.put(
						status.getStatus().name(),
						status.getTicketCount()
				));

		return distribution;
	}

	private Map<String, Long> ticketPriorityDistribution() {
		Map<String, Long> distribution = new LinkedHashMap<>();
		Arrays.stream(TicketPriority.values())
				.forEach(priority -> distribution.put(priority.name(), 0L));

		ticketRepository.countByPriorityDistribution()
				.forEach(priority -> distribution.put(
						priority.getPriority().name(),
						priority.getTicketCount()
				));

		return distribution;
	}
}
