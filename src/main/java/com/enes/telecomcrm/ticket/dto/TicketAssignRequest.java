package com.enes.telecomcrm.ticket.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ticket assignment request")
public record TicketAssignRequest(
		@Schema(description = "Support agent user id", example = "2")
		@NotNull Long agentId
) {
}
