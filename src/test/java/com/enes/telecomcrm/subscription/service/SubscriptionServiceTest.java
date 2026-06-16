package com.enes.telecomcrm.subscription.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.subscription.dto.PlanResponse;
import com.enes.telecomcrm.subscription.dto.SubscriptionRequest;
import com.enes.telecomcrm.subscription.dto.SubscriptionResponse;
import com.enes.telecomcrm.subscription.entity.Plan;
import com.enes.telecomcrm.subscription.entity.PlanType;
import com.enes.telecomcrm.subscription.entity.Subscription;
import com.enes.telecomcrm.subscription.entity.SubscriptionStatus;
import com.enes.telecomcrm.subscription.exception.PlanNotFoundException;
import com.enes.telecomcrm.subscription.exception.SubscriptionNotFoundException;
import com.enes.telecomcrm.subscription.mapper.SubscriptionMapper;
import com.enes.telecomcrm.subscription.repository.PlanRepository;
import com.enes.telecomcrm.subscription.repository.SubscriptionRepository;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.exception.UserNotFoundException;

import jakarta.persistence.EntityManager;

class SubscriptionServiceTest {

	private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
	private final PlanRepository planRepository = mock(PlanRepository.class);
	private final SubscriptionMapper subscriptionMapper = mock(SubscriptionMapper.class);
	private final EntityManager entityManager = mock(EntityManager.class);
	private final SubscriptionService subscriptionService = new SubscriptionService(
			subscriptionRepository,
			planRepository,
			subscriptionMapper,
			entityManager
	);

	@Test
	void createSubscription_createsActiveSubscriptionAndReturnsMappedResponse() {
		SubscriptionRequest request = new SubscriptionRequest(1L, 2L, LocalDate.of(2026, 6, 15));
		User user = user(1L);
		Plan plan = plan(2L);
		Subscription mappedSubscription = subscription(null, user, plan, null);
		Subscription savedSubscription = subscription(10L, user, plan, SubscriptionStatus.ACTIVE);
		SubscriptionResponse response = response(10L, "ACTIVE");

		when(entityManager.find(User.class, 1L)).thenReturn(user);
		when(planRepository.findById(2L)).thenReturn(Optional.of(plan));
		when(subscriptionRepository.existsByUserIdAndPlanIdAndStatus(1L, 2L, SubscriptionStatus.ACTIVE)).thenReturn(false);
		when(subscriptionMapper.toEntity(request)).thenReturn(mappedSubscription);
		when(subscriptionRepository.save(mappedSubscription)).thenReturn(savedSubscription);
		when(subscriptionMapper.toResponse(savedSubscription)).thenReturn(response);

		assertEquals(response, subscriptionService.createSubscription(request));
		assertEquals(user, mappedSubscription.getUser());
		assertEquals(plan, mappedSubscription.getPlan());
		assertEquals(SubscriptionStatus.ACTIVE, mappedSubscription.getStatus());
		assertNull(mappedSubscription.getEndDate());
	}

	@Test
	void createSubscription_whenUserDoesNotExistThrowsUserNotFoundException() {
		SubscriptionRequest request = new SubscriptionRequest(1L, 2L, LocalDate.of(2026, 6, 15));
		when(entityManager.find(User.class, 1L)).thenReturn(null);

		assertThrows(UserNotFoundException.class, () -> subscriptionService.createSubscription(request));
	}

	@Test
	void createSubscription_whenPlanDoesNotExistThrowsPlanNotFoundException() {
		SubscriptionRequest request = new SubscriptionRequest(1L, 2L, LocalDate.of(2026, 6, 15));
		when(entityManager.find(User.class, 1L)).thenReturn(user(1L));
		when(planRepository.findById(2L)).thenReturn(Optional.empty());

		assertThrows(PlanNotFoundException.class, () -> subscriptionService.createSubscription(request));
	}

	@Test
	void createSubscription_whenActiveSubscriptionExistsThrowsBusinessRuleException() {
		SubscriptionRequest request = new SubscriptionRequest(1L, 2L, LocalDate.of(2026, 6, 15));
		when(entityManager.find(User.class, 1L)).thenReturn(user(1L));
		when(planRepository.findById(2L)).thenReturn(Optional.of(plan(2L)));
		when(subscriptionRepository.existsByUserIdAndPlanIdAndStatus(1L, 2L, SubscriptionStatus.ACTIVE)).thenReturn(true);

		assertThrows(BusinessRuleException.class, () -> subscriptionService.createSubscription(request));
	}

	@Test
	void getAllSubscriptions_returnsMappedResponses() {
		List<Subscription> subscriptions = List.of(subscription(1L, user(1L), plan(2L), SubscriptionStatus.ACTIVE));
		List<SubscriptionResponse> responses = List.of(response(1L, "ACTIVE"));

		when(subscriptionRepository.findAll()).thenReturn(subscriptions);
		when(subscriptionMapper.toResponseList(subscriptions)).thenReturn(responses);

		assertEquals(responses, subscriptionService.getAllSubscriptions());
	}

	@Test
	void getSubscriptionById_whenExistsReturnsMappedResponse() {
		Subscription subscription = subscription(1L, user(1L), plan(2L), SubscriptionStatus.ACTIVE);
		SubscriptionResponse response = response(1L, "ACTIVE");

		when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
		when(subscriptionMapper.toResponse(subscription)).thenReturn(response);

		assertEquals(response, subscriptionService.getSubscriptionById(1L));
	}

	@Test
	void getSubscriptionById_whenMissingThrowsSubscriptionNotFoundException() {
		when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(SubscriptionNotFoundException.class, () -> subscriptionService.getSubscriptionById(99L));
	}

	@Test
	void getSubscriptionsByUserId_whenUserExistsReturnsMappedResponses() {
		User user = user(1L);
		List<Subscription> subscriptions = List.of(subscription(1L, user, plan(2L), SubscriptionStatus.ACTIVE));
		List<SubscriptionResponse> responses = List.of(response(1L, "ACTIVE"));

		when(entityManager.find(User.class, 1L)).thenReturn(user);
		when(subscriptionRepository.findByUserId(1L)).thenReturn(subscriptions);
		when(subscriptionMapper.toResponseList(subscriptions)).thenReturn(responses);

		assertEquals(responses, subscriptionService.getSubscriptionsByUserId(1L));
	}

	@Test
	void suspendSubscription_changesActiveSubscriptionToSuspended() {
		Subscription subscription = subscription(1L, user(1L), plan(2L), SubscriptionStatus.ACTIVE);
		SubscriptionResponse response = response(1L, "SUSPENDED");

		when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
		when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(response);

		assertEquals(response, subscriptionService.suspendSubscription(1L));

		ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
		verify(subscriptionRepository).save(captor.capture());
		assertEquals(SubscriptionStatus.SUSPENDED, captor.getValue().getStatus());
	}

	@Test
	void cancelSubscription_changesActiveSubscriptionToCancelledAndSetsEndDate() {
		Subscription subscription = subscription(1L, user(1L), plan(2L), SubscriptionStatus.ACTIVE);
		SubscriptionResponse response = response(1L, "CANCELLED");

		when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
		when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(response);

		assertEquals(response, subscriptionService.cancelSubscription(1L));
		assertEquals(SubscriptionStatus.CANCELLED, subscription.getStatus());
		assertEquals(LocalDate.now(), subscription.getEndDate());
	}

	@Test
	void activateSubscription_changesSuspendedSubscriptionToActive() {
		Subscription subscription = subscription(1L, user(1L), plan(2L), SubscriptionStatus.SUSPENDED);
		subscription.setEndDate(LocalDate.now());
		SubscriptionResponse response = response(1L, "ACTIVE");

		when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
		when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(response);

		assertEquals(response, subscriptionService.activateSubscription(1L));
		assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
		assertNull(subscription.getEndDate());
	}

	@Test
	void activateSubscription_whenCancelledThrowsBusinessRuleException() {
		Subscription subscription = subscription(1L, user(1L), plan(2L), SubscriptionStatus.CANCELLED);
		when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

		BusinessRuleException exception = assertThrows(
				BusinessRuleException.class,
				() -> subscriptionService.activateSubscription(1L)
		);
		assertEquals("Cancelled subscription cannot become active", exception.getMessage());
	}

	@Test
	void suspendSubscription_whenCancelledThrowsBusinessRuleException() {
		Subscription subscription = subscription(1L, user(1L), plan(2L), SubscriptionStatus.CANCELLED);
		when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

		assertThrows(BusinessRuleException.class, () -> subscriptionService.suspendSubscription(1L));
	}

	private User user(Long id) {
		return User.builder()
				.id(id)
				.firstName("John")
				.lastName("Doe")
				.email("john@example.com")
				.password("hashed-password")
				.role(Role.ROLE_USER)
				.build();
	}

	private Plan plan(Long id) {
		return Plan.builder()
				.id(id)
				.name("Mobile Starter")
				.type(PlanType.MOBILE)
				.monthlyPrice(new BigDecimal("199.99"))
				.description("Basic mobile package")
				.build();
	}

	private Subscription subscription(Long id, User user, Plan plan, SubscriptionStatus status) {
		return Subscription.builder()
				.id(id)
				.status(status)
				.startDate(LocalDate.of(2026, 6, 15))
				.user(user)
				.plan(plan)
				.build();
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
