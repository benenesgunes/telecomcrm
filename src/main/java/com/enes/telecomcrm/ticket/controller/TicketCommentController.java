package com.enes.telecomcrm.ticket.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enes.telecomcrm.common.dto.ApiResponse;
import com.enes.telecomcrm.ticket.dto.TicketCommentRequest;
import com.enes.telecomcrm.ticket.dto.TicketCommentResponse;
import com.enes.telecomcrm.ticket.service.TicketCommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
public class TicketCommentController {

	private final TicketCommentService ticketCommentService;

	public TicketCommentController(TicketCommentService ticketCommentService) {
		this.ticketCommentService = ticketCommentService;
	}

	@PostMapping
	@PreAuthorize("@authorizationService.canAccessTicketComments(#ticketId)")
	public ApiResponse<TicketCommentResponse> addComment(
			@PathVariable Long ticketId,
			@Valid @RequestBody TicketCommentRequest request
	) {
		return ApiResponse.success("Ticket comment added successfully", ticketCommentService.addComment(ticketId, request));
	}

	@GetMapping
	@PreAuthorize("@authorizationService.canAccessTicketComments(#ticketId)")
	public ApiResponse<List<TicketCommentResponse>> getComments(@PathVariable Long ticketId) {
		return ApiResponse.success("Ticket comments retrieved successfully", ticketCommentService.getComments(ticketId));
	}
}
