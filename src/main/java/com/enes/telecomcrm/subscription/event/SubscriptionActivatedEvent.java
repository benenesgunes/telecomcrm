package com.enes.telecomcrm.subscription.event;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionActivatedEvent(
		UUID eventId,
		String eventType,
		Instant timestamp,
		SubscriptionActivatedPayload payload
) {

	public static SubscriptionActivatedEvent of(SubscriptionActivatedPayload payload) {
		return new SubscriptionActivatedEvent(UUID.randomUUID(), "SUBSCRIPTION_ACTIVATED", Instant.now(), payload);
	}
}
