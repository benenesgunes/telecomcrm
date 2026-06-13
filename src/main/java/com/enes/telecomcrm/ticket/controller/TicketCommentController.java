package com.enes.telecomcrm.ticket.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enes.telecomcrm.common.dto.ApiResponse;
import com.enes.telecomcrm.ticket.dto.TicketCommentRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
public class TicketCommentController {

	@PostMapping
	@PreAuthorize("@authorizationService.canAccessTicketComments(#ticketId)")
	public ApiResponse<Void> addComment(
			@PathVariable Long ticketId,
			@Valid @RequestBody TicketCommentRequest request
	) {
		return ApiResponse.success("Ticket comment added successfully", null);
	}

	@GetMapping
	@PreAuthorize("@authorizationService.canAccessTicketComments(#ticketId)")
	public ApiResponse<Void> getComments(@PathVariable Long ticketId) {
		return ApiResponse.success("Ticket comments retrieved successfully", null);
	}
}
