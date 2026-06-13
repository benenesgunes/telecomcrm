package com.enes.telecomcrm.ticket.controller;

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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

	@PostMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ApiResponse<Void> createTicket(@Valid @RequestBody TicketRequest request) {
		return ApiResponse.success("Ticket created successfully", null);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	public ApiResponse<Void> getAllTickets() {
		return ApiResponse.success("Tickets retrieved successfully", null);
	}

	@GetMapping("/my")
	@PreAuthorize("hasRole('USER')")
	public ApiResponse<Void> getMyTickets() {
		return ApiResponse.success("User tickets retrieved successfully", null);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.canAccessTicket(#id)")
	public ApiResponse<Void> getTicketById(@PathVariable Long id) {
		return ApiResponse.success("Ticket retrieved successfully", null);
	}

	@PatchMapping("/{id}/assign")
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	public ApiResponse<Void> assignTicket(@PathVariable Long id, @Valid @RequestBody TicketAssignRequest request) {
		return ApiResponse.success("Ticket assigned successfully", null);
	}

	@PatchMapping("/{id}/resolve")
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	public ApiResponse<Void> resolveTicket(@PathVariable Long id) {
		return ApiResponse.success("Ticket resolved successfully", null);
	}

	@PatchMapping("/{id}/close")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> closeTicket(@PathVariable Long id) {
		return ApiResponse.success("Ticket closed successfully", null);
	}
}
