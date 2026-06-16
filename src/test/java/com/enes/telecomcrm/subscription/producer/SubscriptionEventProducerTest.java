package com.enes.telecomcrm.subscription.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import com.enes.telecomcrm.subscription.entity.Plan;
import com.enes.telecomcrm.subscription.entity.PlanType;
import com.enes.telecomcrm.subscription.entity.Subscription;
import com.enes.telecomcrm.subscription.entity.SubscriptionStatus;
import com.enes.telecomcrm.subscription.event.SubscriptionActivatedEvent;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;

class SubscriptionEventProducerTest {

	private final KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
	private final SubscriptionEventProducer subscriptionEventProducer = new SubscriptionEventProducer(
			kafkaTemplate,
			"subscription-activated",
			true
	);

	@Test
	void publishSubscriptionActivated_sendsSubscriptionActivatedEventToConfiguredTopic() {
		Subscription subscription = subscription();
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

		subscriptionEventProducer.publishSubscriptionActivated(subscription);

		verify(kafkaTemplate).send(eq("subscription-activated"), eq("22"), eventCaptor.capture());
		SubscriptionActivatedEvent event = (SubscriptionActivatedEvent) eventCaptor.getValue();
		assertNotNull(event.eventId());
		assertEquals("SUBSCRIPTION_ACTIVATED", event.eventType());
		assertEquals(22L, event.payload().subscriptionId());
		assertEquals(5L, event.payload().userId());
		assertEquals("john.doe@example.com", event.payload().userEmail());
		assertEquals(3L, event.payload().planId());
		assertEquals("Home Internet 100Mbps", event.payload().planName());
		assertEquals(LocalDate.of(2024, 2, 1), event.payload().startDate());
	}

	@Test
	void publishSubscriptionActivated_whenKafkaDisabledDoesNotSendEvent() {
		SubscriptionEventProducer disabledProducer = new SubscriptionEventProducer(
				kafkaTemplate,
				"subscription-activated",
				false
		);

		disabledProducer.publishSubscriptionActivated(subscription());

		verify(kafkaTemplate, never()).send(eq("subscription-activated"), eq("22"), org.mockito.ArgumentMatchers.any());
	}

	private Subscription subscription() {
		User user = User.builder()
				.id(5L)
				.firstName("John")
				.lastName("Doe")
				.email("john.doe@example.com")
				.password("hashed-password")
				.role(Role.ROLE_USER)
				.build();
		Plan plan = Plan.builder()
				.id(3L)
				.name("Home Internet 100Mbps")
				.type(PlanType.INTERNET)
				.monthlyPrice(new BigDecimal("399.99"))
				.build();
		return Subscription.builder()
				.id(22L)
				.user(user)
				.plan(plan)
				.status(SubscriptionStatus.ACTIVE)
				.startDate(LocalDate.of(2024, 2, 1))
				.build();
	}
}
