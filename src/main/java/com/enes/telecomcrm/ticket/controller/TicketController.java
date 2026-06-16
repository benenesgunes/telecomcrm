package com.enes.telecomcrm.ticket.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enes.telecomcrm.common.dto.ApiResponse;
import com.enes.telecomcrm.ticket.dto.TicketAssignRequest;
import com.enes.telecomcrm.ticket.dto.TicketRequest;
import com.enes.telecomcrm.ticket.dto.TicketResponse;
import com.enes.telecomcrm.ticket.service.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Support ticket lifecycle endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

	private final TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Create ticket", description = "Creates a support ticket. Requires an active subscription.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket created successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body or missing active subscription")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<TicketResponse> createTicket(@Valid @RequestBody TicketRequest request) {
		return ApiResponse.success("Ticket created successfully", ticketService.createTicket(request));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	@Operation(summary = "List tickets", description = "Returns all tickets. Requires ROLE_SUPPORT_AGENT or ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tickets retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<List<TicketResponse>> getAllTickets() {
		return ApiResponse.success("Tickets retrieved successfully", ticketService.getAllTickets());
	}

	@GetMapping("/my")
	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "List my tickets", description = "Returns tickets created by the authenticated customer.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User tickets retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<List<TicketResponse>> getMyTickets() {
		return ApiResponse.success("User tickets retrieved successfully", ticketService.getMyTickets());
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.canAccessTicket(#id)")
	@Operation(summary = "Get ticket", description = "Returns a ticket by id for the owner, assigned agent, or admin.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket retrieved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket not found")
	public ApiResponse<TicketResponse> getTicketById(@PathVariable Long id) {
		return ApiResponse.success("Ticket retrieved successfully", ticketService.getTicketById(id));
	}

	@PatchMapping("/{id}/assign")
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	@Operation(summary = "Assign ticket", description = "Assigns an open ticket to a support agent or admin.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket assigned successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Only open tickets are assignable")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket or agent not found")
	public ApiResponse<TicketResponse> assignTicket(@PathVariable Long id, @Valid @RequestBody TicketAssignRequest request) {
		return ApiResponse.success("Ticket assigned successfully", ticketService.assignTicket(id, request));
	}

	@PatchMapping("/{id}/resolve")
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	@Operation(summary = "Resolve ticket", description = "Marks a ticket as resolved. Closed tickets cannot be modified.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket resolved successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Closed ticket cannot be modified")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket not found")
	public ApiResponse<TicketResponse> resolveTicket(@PathVariable Long id) {
		return ApiResponse.success("Ticket resolved successfully", ticketService.resolveTicket(id));
	}

	@PatchMapping("/{id}/close")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Close ticket", description = "Closes a ticket. Requires ROLE_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket closed successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Closed ticket cannot be modified")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket not found")
	public ApiResponse<TicketResponse> closeTicket(@PathVariable Long id) {
		return ApiResponse.success("Ticket closed successfully", ticketService.closeTicket(id));
	}
}
