package com.enes.telecomcrm.subscription.exception;

import com.enes.telecomcrm.common.exception.ResourceNotFoundException;

public class SubscriptionNotFoundException extends ResourceNotFoundException {

	public SubscriptionNotFoundException(Long id) {
		super("Subscription not found with id: " + id);
	}
}
