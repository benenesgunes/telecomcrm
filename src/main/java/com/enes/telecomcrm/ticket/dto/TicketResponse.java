package com.enes.telecomcrm.ticket.dto;

import java.time.LocalDateTime;

import com.enes.telecomcrm.user.dto.UserResponse;

public record TicketResponse(
		Long id,
		String title,
		String description,
		String status,
		String priority,
		UserResponse customer,
		UserResponse assignedAgent,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
