package com.enes.telecomcrm.subscription.event;

import java.time.LocalDate;

public record SubscriptionActivatedPayload(
		Long subscriptionId,
		Long userId,
		String userEmail,
		Long planId,
		String planName,
		LocalDate startDate
) {
}
