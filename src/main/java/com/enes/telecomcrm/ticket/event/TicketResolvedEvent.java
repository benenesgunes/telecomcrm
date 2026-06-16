package com.enes.telecomcrm.ticket.event;

import java.time.Instant;
import java.util.UUID;

public record TicketResolvedEvent(
		UUID eventId,
		String eventType,
		Instant timestamp,
		TicketResolvedPayload payload
) {

	public static TicketResolvedEvent of(TicketResolvedPayload payload) {
		return new TicketResolvedEvent(UUID.randomUUID(), "TICKET_RESOLVED", Instant.now(), payload);
	}
}
