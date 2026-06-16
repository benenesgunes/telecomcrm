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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
@Tag(name = "Ticket Comments", description = "Ticket comment endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TicketCommentController {

	private final TicketCommentService ticketCommentService;

	public TicketCommentController(TicketCommentService ticketCommentService) {
		this.ticketCommentService = ticketCommentService;
	}

	@PostMapping
	@PreAuthorize("@authorizationService.canAccessTicketComments(#ticketId)")
	@Operation(summary = "Add ticket comment", description = "Adds a comment as the owner, assigned agent, or admin. Closed tickets reject new comments.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket comment added successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body or closed ticket")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket not found")
	public ApiResponse<TicketCommentResponse> addComment(
			@PathVariable Long ticketId,
			@Valid @RequestBody TicketCommentRequest request
	) {
		return ApiResponse.success("Ticket comment added successfully", ticketCommentService.addComment(ticketId, request));
	}

	@GetMapping
	@PreAuthorize("@authorizationService.canAccessTicketComments(#ticketId)")
	@Operation(summary = "List ticket comments", description = "Returns comments for a ticket as the owner, assigned agent, or admin.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket comments retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket not found")
	public ApiResponse<List<TicketCommentResponse>> getComments(@PathVariable Long ticketId) {
		return ApiResponse.success("Ticket comments retrieved successfully", ticketCommentService.getComments(ticketId));
	}
}
