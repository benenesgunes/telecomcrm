package com.enes.telecomcrm.ticket.event;

public record TicketResolvedPayload(
		Long ticketId,
		Long resolvedByAgentId,
		long resolutionTimeMinutes,
		Long customerId,
		String customerEmail
) {
}
