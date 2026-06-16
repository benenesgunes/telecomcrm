package com.enes.telecomcrm.ticket.event;

import java.time.Instant;
import java.util.UUID;

public record TicketCreatedEvent(
		UUID eventId,
		String eventType,
		Instant timestamp,
		TicketCreatedPayload payload
) {

	public static TicketCreatedEvent of(TicketCreatedPayload payload) {
		return new TicketCreatedEvent(UUID.randomUUID(), "TICKET_CREATED", Instant.now(), payload);
	}
}
