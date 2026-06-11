package com.enes.telecomcrm.ticket.dto;

import com.enes.telecomcrm.ticket.entity.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketRequest(
		@NotBlank @Size(min = 5, max = 255) String title,
		@NotBlank @Size(min = 10) String description,
		@NotNull TicketPriority priority
) {
}
