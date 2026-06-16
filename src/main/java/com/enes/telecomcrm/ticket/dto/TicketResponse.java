package com.enes.telecomcrm.ticket.dto;

import java.time.LocalDateTime;

import com.enes.telecomcrm.user.dto.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ticket response")
public record TicketResponse(
		@Schema(description = "Ticket id", example = "10")
		Long id,
		@Schema(description = "Ticket title", example = "Internet drops")
		String title,
		@Schema(description = "Detailed issue description", example = "Connection drops every morning.")
		String description,
		@Schema(description = "Ticket status", example = "OPEN", allowableValues = {"OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"})
		String status,
		@Schema(description = "Ticket priority", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
		String priority,
		@Schema(description = "Customer who created the ticket")
		UserResponse customer,
		@Schema(description = "Assigned support agent")
		UserResponse assignedAgent,
		@Schema(description = "Creation timestamp", example = "2026-06-16T10:15:30")
		LocalDateTime createdAt,
		@Schema(description = "Last update timestamp", example = "2026-06-16T10:20:30")
		LocalDateTime updatedAt
) {
}
