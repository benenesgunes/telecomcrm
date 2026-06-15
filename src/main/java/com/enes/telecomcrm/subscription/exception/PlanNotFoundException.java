package com.enes.telecomcrm.subscription.exception;

import com.enes.telecomcrm.common.exception.ResourceNotFoundException;

public class PlanNotFoundException extends ResourceNotFoundException {

	public PlanNotFoundException(Long id) {
		super("Plan not found with id: " + id);
	}
}
