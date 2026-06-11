package com.enes.telecomcrm.ticket.dto;

import java.time.LocalDateTime;

import com.enes.telecomcrm.user.dto.UserResponse;

public record TicketCommentResponse(
		Long id,
		String message,
		UserResponse author,
		Long ticketId,
		LocalDateTime createdAt
) {
}
