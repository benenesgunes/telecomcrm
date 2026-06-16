package com.enes.telecomcrm.ticket.event;

public record TicketCreatedPayload(
		Long ticketId,
		String title,
		String priority,
		Long customerId,
		String customerEmail
) {
}
