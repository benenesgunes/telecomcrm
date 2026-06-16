package com.enes.telecomcrm.subscription.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.enes.telecomcrm.subscription.entity.Subscription;
import com.enes.telecomcrm.subscription.event.SubscriptionActivatedEvent;
import com.enes.telecomcrm.subscription.event.SubscriptionActivatedPayload;

@Component
public class SubscriptionEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final String subscriptionActivatedTopic;

	public SubscriptionEventProducer(
			KafkaTemplate<String, Object> kafkaTemplate,
			@Value("${app.kafka.topics.subscription-activated:subscription-activated}") String subscriptionActivatedTopic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.subscriptionActivatedTopic = subscriptionActivatedTopic;
	}

	public void publishSubscriptionActivated(Subscription subscription) {
		SubscriptionActivatedPayload payload = new SubscriptionActivatedPayload(
				subscription.getId(),
				subscription.getUser().getId(),
				subscription.getUser().getEmail(),
				subscription.getPlan().getId(),
				subscription.getPlan().getName(),
				subscription.getStartDate()
		);
		kafkaTemplate.send(
				subscriptionActivatedTopic,
				String.valueOf(subscription.getId()),
				SubscriptionActivatedEvent.of(payload)
		);
	}
}
