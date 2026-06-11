package com.enes.telecomcrm.ticket.dto;

import jakarta.validation.constraints.NotNull;

public record TicketAssignRequest(
		@NotNull Long agentId
) {
}
