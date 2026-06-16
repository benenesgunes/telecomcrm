package com.enes.telecomcrm.ticket.exception;

import com.enes.telecomcrm.common.exception.ResourceNotFoundException;

public class TicketNotFoundException extends ResourceNotFoundException {

	public TicketNotFoundException(Long id) {
		super("Ticket not found with id: " + id);
	}
}
