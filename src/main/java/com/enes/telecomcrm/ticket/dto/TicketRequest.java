package com.enes.telecomcrm.ticket.dto;

import com.enes.telecomcrm.ticket.entity.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ticket create request")
public record TicketRequest(
		@Schema(description = "Ticket title", example = "Internet drops")
		@NotBlank @Size(min = 5, max = 255) String title,
		@Schema(description = "Detailed issue description", example = "Connection drops every morning.")
		@NotBlank @Size(min = 10) String description,
		@Schema(description = "Ticket priority", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
		@NotNull TicketPriority priority
) {
}
