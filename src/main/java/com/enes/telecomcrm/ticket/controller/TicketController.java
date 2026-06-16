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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

	private final TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ApiResponse<TicketResponse> createTicket(@Valid @RequestBody TicketRequest request) {
		return ApiResponse.success("Ticket created successfully", ticketService.createTicket(request));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	public ApiResponse<List<TicketResponse>> getAllTickets() {
		return ApiResponse.success("Tickets retrieved successfully", ticketService.getAllTickets());
	}

	@GetMapping("/my")
	@PreAuthorize("hasRole('USER')")
	public ApiResponse<List<TicketResponse>> getMyTickets() {
		return ApiResponse.success("User tickets retrieved successfully", ticketService.getMyTickets());
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.canAccessTicket(#id)")
	public ApiResponse<TicketResponse> getTicketById(@PathVariable Long id) {
		return ApiResponse.success("Ticket retrieved successfully", ticketService.getTicketById(id));
	}

	@PatchMapping("/{id}/assign")
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	public ApiResponse<TicketResponse> assignTicket(@PathVariable Long id, @Valid @RequestBody TicketAssignRequest request) {
		return ApiResponse.success("Ticket assigned successfully", ticketService.assignTicket(id, request));
	}

	@PatchMapping("/{id}/resolve")
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	public ApiResponse<TicketResponse> resolveTicket(@PathVariable Long id) {
		return ApiResponse.success("Ticket resolved successfully", ticketService.resolveTicket(id));
	}

	@PatchMapping("/{id}/close")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<TicketResponse> closeTicket(@PathVariable Long id) {
		return ApiResponse.success("Ticket closed successfully", ticketService.closeTicket(id));
	}
}
