package com.enes.telecomcrm.user.exception;

import com.enes.telecomcrm.common.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {

	public UserNotFoundException(Long id) {
		super("User not found with id: " + id);
	}
}
