package com.enes.telecomcrm.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketCommentRequest(
		@NotBlank @Size(min = 1, max = 2000) String message
) {
}
