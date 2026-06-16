package com.enes.telecomcrm.ticket.dto;

import java.time.LocalDateTime;

import com.enes.telecomcrm.user.dto.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ticket comment response")
public record TicketCommentResponse(
		@Schema(description = "Comment id", example = "1")
		Long id,
		@Schema(description = "Comment message", example = "Looking into this issue.")
		String message,
		@Schema(description = "Comment author")
		UserResponse author,
		@Schema(description = "Ticket id", example = "10")
		Long ticketId,
		@Schema(description = "Creation timestamp", example = "2026-06-16T10:15:30")
		LocalDateTime createdAt
) {
}
