package com.enes.telecomcrm.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ticket comment request")
public record TicketCommentRequest(
		@Schema(description = "Comment message", example = "Looking into this issue.")
		@NotBlank @Size(min = 1, max = 2000) String message
) {
}
