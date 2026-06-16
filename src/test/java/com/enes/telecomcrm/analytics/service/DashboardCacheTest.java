package com.enes.telecomcrm.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.enes.telecomcrm.auth.security.UserPrincipal;
import com.enes.telecomcrm.search.service.TicketSearchIndexService;
import com.enes.telecomcrm.subscription.dto.SubscriptionRequest;
import com.enes.telecomcrm.subscription.dto.SubscriptionResponse;
import com.enes.telecomcrm.subscription.entity.Plan;
import com.enes.telecomcrm.subscription.entity.PlanType;
import com.enes.telecomcrm.subscription.entity.Subscription;
import com.enes.telecomcrm.subscription.entity.SubscriptionStatus;
import com.enes.telecomcrm.subscription.mapper.SubscriptionMapper;
import com.enes.telecomcrm.subscription.producer.SubscriptionEventProducer;
import com.enes.telecomcrm.subscription.repository.PlanRepository;
import com.enes.telecomcrm.subscription.repository.SubscriptionRepository;
import com.enes.telecomcrm.subscription.service.SubscriptionService;
import com.enes.telecomcrm.ticket.dto.TicketAssignRequest;
import com.enes.telecomcrm.ticket.dto.TicketRequest;
import com.enes.telecomcrm.ticket.dto.TicketResponse;
import com.enes.telecomcrm.ticket.entity.Ticket;
import com.enes.telecomcrm.ticket.entity.TicketPriority;
import com.enes.telecomcrm.ticket.entity.TicketStatus;
import com.enes.telecomcrm.ticket.mapper.TicketMapper;
import com.enes.telecomcrm.ticket.producer.TicketEventProducer;
import com.enes.telecomcrm.ticket.repository.TicketRepository;
import com.enes.telecomcrm.ticket.service.TicketService;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringJUnitConfig(DashboardCacheTest.CacheTestConfig.class)
class DashboardCacheTest {

	@jakarta.annotation.Resource
	private DashboardService dashboardService;

	@jakarta.annotation.Resource
	private TicketService ticketService;

	@jakarta.annotation.Resource
	private SubscriptionService subscriptionService;

	@jakarta.annotation.Resource
	private UserRepository userRepository;

	@jakarta.annotation.Resource
	private SubscriptionRepository subscriptionRepository;

	@jakarta.annotation.Resource
	private PlanRepository planRepository;

	@jakarta.annotation.Resource
	private TicketRepository ticketRepository;

	@jakarta.annotation.Resource
	private TicketMapper ticketMapper;

	@jakarta.annotation.Resource
	private SubscriptionMapper subscriptionMapper;

	@jakarta.annotation.Resource
	private EntityManager entityManager;

	@jakarta.annotation.Resource
	private CacheManager cacheManager;

	@BeforeEach
	void setUp() {
		reset(userRepository, subscriptionRepository, planRepository, ticketRepository, ticketMapper, subscriptionMapper, entityManager);
		cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void dashboardRequestsUseCacheOnSecondCall() {
		when(userRepository.count()).thenReturn(42L);
		when(subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE)).thenReturn(18L);
		when(subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED)).thenReturn(4L);
		when(ticketRepository.countByStatus(TicketStatus.OPEN)).thenReturn(7L);
		when(ticketRepository.countByStatus(TicketStatus.RESOLVED)).thenReturn(11L);

		assertEquals(42L, dashboardService.getAdminMetrics().totalUsers());
		assertEquals(42L, dashboardService.getAdminMetrics().totalUsers());

		verify(userRepository, times(1)).count();
		verify(subscriptionRepository, times(1)).countByStatus(SubscriptionStatus.ACTIVE);
		verify(ticketRepository, times(1)).countByStatus(TicketStatus.OPEN);
	}

	@Test
	void ticketCreatedEvictsAdminAndTicketDashboards() {
		authenticate(1L, Role.ROLE_USER);
		User customer = user(1L, Role.ROLE_USER);
		TicketRequest request = new TicketRequest("Internet drops", "Connection drops every morning.", TicketPriority.HIGH);
		Ticket mappedTicket = ticket(null, customer, null, TicketStatus.OPEN);
		Ticket savedTicket = ticket(10L, customer, null, TicketStatus.OPEN);

		stubAdminMetrics();
		stubTicketMetrics();
		when(entityManager.find(User.class, 1L)).thenReturn(customer);
		when(subscriptionRepository.existsByUserIdAndStatus(1L, SubscriptionStatus.ACTIVE)).thenReturn(true);
		when(ticketMapper.toEntity(request)).thenReturn(mappedTicket);
		when(ticketRepository.save(mappedTicket)).thenReturn(savedTicket);
		when(ticketMapper.toResponse(savedTicket)).thenReturn(ticketResponse(10L, "OPEN", null));

		dashboardService.getAdminMetrics();
		dashboardService.getTicketMetrics();
		ticketService.createTicket(request);
		dashboardService.getAdminMetrics();
		dashboardService.getTicketMetrics();

		verify(userRepository, times(2)).count();
		verify(ticketRepository, times(2)).countByStatusDistribution();
	}

	@Test
	void subscriptionCreatedEvictsAdminSubscriptionAndPopularPlanCaches() {
		User user = user(1L, Role.ROLE_USER);
		Plan plan = plan(1L);
		SubscriptionRequest request = new SubscriptionRequest(1L, 1L, LocalDate.now());
		Subscription mappedSubscription = subscription(null, user, plan, SubscriptionStatus.ACTIVE);
		Subscription savedSubscription = subscription(20L, user, plan, SubscriptionStatus.ACTIVE);

		stubAdminMetrics();
		stubSubscriptionMetrics();
		when(entityManager.find(User.class, 1L)).thenReturn(user);
		when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
		when(subscriptionRepository.existsByUserIdAndPlanIdAndStatus(1L, 1L, SubscriptionStatus.ACTIVE)).thenReturn(false);
		when(subscriptionMapper.toEntity(request)).thenReturn(mappedSubscription);
		when(subscriptionRepository.save(mappedSubscription)).thenReturn(savedSubscription);
		when(subscriptionMapper.toResponse(savedSubscription)).thenReturn(subscriptionResponse(20L));

		dashboardService.getAdminMetrics();
		dashboardService.getSubscriptionMetrics();
		subscriptionService.createSubscription(request);
		dashboardService.getAdminMetrics();
		dashboardService.getSubscriptionMetrics();

		verify(userRepository, times(2)).count();
		verify(planRepository, times(2)).findPopularPlans();
	}

	@Test
	void ticketAssignmentEvictsAssignedAgentDashboard() {
		authenticate(2L, Role.ROLE_SUPPORT_AGENT);
		User customer = user(1L, Role.ROLE_USER);
		User agent = user(2L, Role.ROLE_SUPPORT_AGENT);
		Ticket ticket = ticket(10L, customer, null, TicketStatus.OPEN);
		TicketAssignRequest request = new TicketAssignRequest(2L);

		stubAgentMetrics(agent);
		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(entityManager.find(User.class, 2L)).thenReturn(agent);
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(ticketResponse(10L, "IN_PROGRESS", agent));

		dashboardService.getAgentMetrics();
		ticketService.assignTicket(10L, request);
		dashboardService.getAgentMetrics();

		verify(userRepository, times(2)).findById(2L);
		verify(ticketRepository, times(2)).countByAssignedAgentId(2L);
	}

	@Test
	void ticketResolvedEvictsAdminTicketAndAssignedAgentDashboards() {
		authenticate(2L, Role.ROLE_SUPPORT_AGENT);
		User customer = user(1L, Role.ROLE_USER);
		User agent = user(2L, Role.ROLE_SUPPORT_AGENT);
		Ticket ticket = ticket(10L, customer, agent, TicketStatus.IN_PROGRESS);

		stubAdminMetrics();
		stubTicketMetrics();
		stubAgentMetrics(agent);
		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(ticketResponse(10L, "RESOLVED", agent));

		dashboardService.getAdminMetrics();
		dashboardService.getTicketMetrics();
		dashboardService.getAgentMetrics();
		ticketService.resolveTicket(10L);
		dashboardService.getAdminMetrics();
		dashboardService.getTicketMetrics();
		dashboardService.getAgentMetrics();

		verify(userRepository, times(2)).count();
		verify(ticketRepository, times(2)).countByStatusDistribution();
		verify(userRepository, times(2)).findById(2L);
	}

	private void stubAdminMetrics() {
		when(userRepository.count()).thenReturn(42L);
		when(subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE)).thenReturn(18L);
		when(subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED)).thenReturn(4L);
		when(ticketRepository.countByStatus(TicketStatus.OPEN)).thenReturn(7L);
		when(ticketRepository.countByStatus(TicketStatus.RESOLVED)).thenReturn(11L);
	}

	private void stubSubscriptionMetrics() {
		when(planRepository.findPopularPlans()).thenReturn(List.of());
		when(subscriptionRepository.countMonthlyGrowthSince(any(LocalDate.class))).thenReturn(List.of());
		when(subscriptionRepository.countByStatusDistribution()).thenReturn(List.of());
	}

	private void stubTicketMetrics() {
		when(ticketRepository.countByStatusDistribution()).thenReturn(List.of());
		when(ticketRepository.countByPriorityDistribution()).thenReturn(List.of());
	}

	private void stubAgentMetrics(User agent) {
		when(userRepository.findById(agent.getId())).thenReturn(Optional.of(agent));
		when(ticketRepository.countByAssignedAgentId(agent.getId())).thenReturn(12L);
		when(ticketRepository.countByAssignedAgentIdAndStatus(agent.getId(), TicketStatus.RESOLVED)).thenReturn(9L);
		when(ticketRepository.averageResolutionTimeHoursByAssignedAgentId(agent.getId())).thenReturn(4.5);
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

	private User user(Long id, Role role) {
		return User.builder()
				.id(id)
				.firstName("John")
				.lastName("Doe")
				.email("user%d@example.com".formatted(id))
				.password("hashed-password")
				.role(role)
				.build();
	}

	private Plan plan(Long id) {
		return Plan.builder()
				.id(id)
				.name("Home Internet")
				.type(PlanType.INTERNET)
				.monthlyPrice(new BigDecimal("399.99"))
				.description("100Mbps home internet package")
				.build();
	}

	private Subscription subscription(Long id, User user, Plan plan, SubscriptionStatus status) {
		return Subscription.builder()
				.id(id)
				.user(user)
				.plan(plan)
				.status(status)
				.startDate(LocalDate.now())
				.build();
	}

	private Ticket ticket(Long id, User customer, User assignedAgent, TicketStatus status) {
		return Ticket.builder()
				.id(id)
				.title("Internet drops")
				.description("Connection drops every morning.")
				.priority(TicketPriority.HIGH)
				.status(status)
				.customer(customer)
				.assignedAgent(assignedAgent)
				.build();
	}

	private TicketResponse ticketResponse(Long id, String status, User assignedAgent) {
		return new TicketResponse(
				id,
				"Internet drops",
				"Connection drops every morning.",
				status,
				"HIGH",
				new UserResponse(1L, "John", "Doe", "user1@example.com", "ROLE_USER", null, null),
				assignedAgent == null ? null : new UserResponse(assignedAgent.getId(), "John", "Doe", assignedAgent.getEmail(), assignedAgent.getRole().name(), null, null),
				null,
				null
		);
	}

	private SubscriptionResponse subscriptionResponse(Long id) {
		return new SubscriptionResponse(
				id,
				"ACTIVE",
				LocalDate.now(),
				null,
				null,
				null
		);
	}

	@Configuration
	@EnableCaching
	static class CacheTestConfig {

		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager(
					"dashboard:admin",
					"dashboard:subscriptions",
					"dashboard:tickets",
					"dashboard:agent",
					"plans:popular"
			);
		}

		@Bean
		UserRepository userRepository() {
			return mock(UserRepository.class);
		}

		@Bean
		SubscriptionRepository subscriptionRepository() {
			return mock(SubscriptionRepository.class);
		}

		@Bean
		PlanRepository planRepository() {
			return mock(PlanRepository.class);
		}

		@Bean
		TicketRepository ticketRepository() {
			return mock(TicketRepository.class);
		}

		@Bean
		TicketMapper ticketMapper() {
			return mock(TicketMapper.class);
		}

		@Bean
		TicketSearchIndexService ticketSearchIndexService() {
			return mock(TicketSearchIndexService.class);
		}

		@Bean
		SubscriptionMapper subscriptionMapper() {
			return mock(SubscriptionMapper.class);
		}

		@Bean
		EntityManager entityManager() {
			return mock(EntityManager.class);
		}

		@Bean
		TicketEventProducer ticketEventProducer() {
			return mock(TicketEventProducer.class);
		}

		@Bean
		SubscriptionEventProducer subscriptionEventProducer() {
			return mock(SubscriptionEventProducer.class);
		}

		@Bean
		DashboardService dashboardService(
				UserRepository userRepository,
				SubscriptionRepository subscriptionRepository,
				PlanRepository planRepository,
				TicketRepository ticketRepository
		) {
			return new DashboardService(userRepository, subscriptionRepository, planRepository, ticketRepository);
		}

		@Bean
		TicketService ticketService(
				TicketRepository ticketRepository,
				SubscriptionRepository subscriptionRepository,
				TicketMapper ticketMapper,
				EntityManager entityManager,
				TicketEventProducer ticketEventProducer,
				TicketSearchIndexService ticketSearchIndexService
		) {
			return new TicketService(
					ticketRepository,
					subscriptionRepository,
					ticketMapper,
					entityManager,
					ticketEventProducer,
					ticketSearchIndexService
			);
		}

		@Bean
		SubscriptionService subscriptionService(
				SubscriptionRepository subscriptionRepository,
				PlanRepository planRepository,
				SubscriptionMapper subscriptionMapper,
				EntityManager entityManager,
				SubscriptionEventProducer subscriptionEventProducer
		) {
			return new SubscriptionService(
					subscriptionRepository,
					planRepository,
					subscriptionMapper,
					entityManager,
					subscriptionEventProducer
			);
		}
	}
}
