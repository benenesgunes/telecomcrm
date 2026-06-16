package com.enes.telecomcrm.subscription.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.subscription.dto.SubscriptionRequest;
import com.enes.telecomcrm.subscription.dto.SubscriptionResponse;
import com.enes.telecomcrm.subscription.entity.Plan;
import com.enes.telecomcrm.subscription.entity.Subscription;
import com.enes.telecomcrm.subscription.entity.SubscriptionStatus;
import com.enes.telecomcrm.subscription.exception.PlanNotFoundException;
import com.enes.telecomcrm.subscription.exception.SubscriptionNotFoundException;
import com.enes.telecomcrm.subscription.mapper.SubscriptionMapper;
import com.enes.telecomcrm.subscription.producer.SubscriptionEventProducer;
import com.enes.telecomcrm.subscription.repository.PlanRepository;
import com.enes.telecomcrm.subscription.repository.SubscriptionRepository;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.exception.UserNotFoundException;

import jakarta.persistence.EntityManager;

@Service
public class SubscriptionService {

	private final SubscriptionRepository subscriptionRepository;
	private final PlanRepository planRepository;
	private final SubscriptionMapper subscriptionMapper;
	private final EntityManager entityManager;
	private final SubscriptionEventProducer subscriptionEventProducer;

	public SubscriptionService(
			SubscriptionRepository subscriptionRepository,
			PlanRepository planRepository,
			SubscriptionMapper subscriptionMapper,
			EntityManager entityManager,
			SubscriptionEventProducer subscriptionEventProducer
	) {
		this.subscriptionRepository = subscriptionRepository;
		this.planRepository = planRepository;
		this.subscriptionMapper = subscriptionMapper;
		this.entityManager = entityManager;
		this.subscriptionEventProducer = subscriptionEventProducer;
	}

	@Transactional
	@CacheEvict(cacheNames = {"dashboard:admin", "dashboard:subscriptions", "plans:popular"}, allEntries = true)
	public SubscriptionResponse createSubscription(SubscriptionRequest request) {
		User user = findUserById(request.userId());
		Plan plan = findPlanById(request.planId());

		if (subscriptionRepository.existsByUserIdAndPlanIdAndStatus(
				request.userId(),
				request.planId(),
				SubscriptionStatus.ACTIVE
		)) {
			throw new BusinessRuleException("User already has an active subscription for this plan");
		}

		Subscription subscription = subscriptionMapper.toEntity(request);
		subscription.setUser(user);
		subscription.setPlan(plan);
		subscription.setStatus(SubscriptionStatus.ACTIVE);
		subscription.setEndDate(null);

		Subscription savedSubscription = subscriptionRepository.save(subscription);
		subscriptionEventProducer.publishSubscriptionActivated(savedSubscription);
		return subscriptionMapper.toResponse(savedSubscription);
	}

	@Transactional(readOnly = true)
	public List<SubscriptionResponse> getAllSubscriptions() {
		return subscriptionMapper.toResponseList(subscriptionRepository.findAll());
	}

	@Transactional(readOnly = true)
	public SubscriptionResponse getSubscriptionById(Long id) {
		return subscriptionMapper.toResponse(findSubscriptionById(id));
	}

	@Transactional(readOnly = true)
	public List<SubscriptionResponse> getSubscriptionsByUserId(Long userId) {
		findUserById(userId);
		return subscriptionMapper.toResponseList(subscriptionRepository.findByUserId(userId));
	}

	@Transactional
	@CacheEvict(cacheNames = {"dashboard:admin", "dashboard:subscriptions"}, allEntries = true)
	public SubscriptionResponse suspendSubscription(Long id) {
		Subscription subscription = findSubscriptionById(id);
		transition(subscription, SubscriptionStatus.SUSPENDED);
		return subscriptionMapper.toResponse(subscriptionRepository.save(subscription));
	}

	@Transactional
	@CacheEvict(cacheNames = {"dashboard:admin", "dashboard:subscriptions"}, allEntries = true)
	public SubscriptionResponse cancelSubscription(Long id) {
		Subscription subscription = findSubscriptionById(id);
		transition(subscription, SubscriptionStatus.CANCELLED);
		return subscriptionMapper.toResponse(subscriptionRepository.save(subscription));
	}

	@Transactional
	@CacheEvict(cacheNames = {"dashboard:admin", "dashboard:subscriptions"}, allEntries = true)
	public SubscriptionResponse activateSubscription(Long id) {
		Subscription subscription = findSubscriptionById(id);
		transition(subscription, SubscriptionStatus.ACTIVE);
		return subscriptionMapper.toResponse(subscriptionRepository.save(subscription));
	}

	private void transition(Subscription subscription, SubscriptionStatus targetStatus) {
		SubscriptionStatus currentStatus = subscription.getStatus();

		if (currentStatus == SubscriptionStatus.CANCELLED && targetStatus == SubscriptionStatus.ACTIVE) {
			throw new BusinessRuleException("Cancelled subscription cannot become active");
		}

		if (currentStatus == SubscriptionStatus.CANCELLED) {
			throw new BusinessRuleException("Cancelled subscription cannot be modified");
		}

		if (currentStatus == targetStatus) {
			return;
		}

		if (currentStatus == SubscriptionStatus.EXPIRED && targetStatus != SubscriptionStatus.CANCELLED) {
			throw new BusinessRuleException("Expired subscription cannot be modified");
		}

		subscription.setStatus(targetStatus);
		if (targetStatus == SubscriptionStatus.CANCELLED || targetStatus == SubscriptionStatus.EXPIRED) {
			subscription.setEndDate(LocalDate.now());
		}
		if (targetStatus == SubscriptionStatus.ACTIVE) {
			subscription.setEndDate(null);
		}
	}

	private Subscription findSubscriptionById(Long id) {
		return subscriptionRepository.findById(id)
				.orElseThrow(() -> new SubscriptionNotFoundException(id));
	}

	private User findUserById(Long id) {
		User user = entityManager.find(User.class, id);
		if (user == null) {
			throw new UserNotFoundException(id);
		}
		return user;
	}

	private Plan findPlanById(Long id) {
		return planRepository.findById(id)
				.orElseThrow(() -> new PlanNotFoundException(id));
	}
}
