package com.enes.telecomcrm.search.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enes.telecomcrm.common.dto.ApiResponse;
import com.enes.telecomcrm.search.service.SearchService;
import com.enes.telecomcrm.ticket.dto.TicketResponse;
import com.enes.telecomcrm.user.dto.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Elasticsearch-backed fuzzy and partial search endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

	private final SearchService searchService;

	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}

	@GetMapping("/tickets")
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	@Operation(summary = "Search tickets", description = "Runs fuzzy and partial search against ticket title and description.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket search completed successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<List<TicketResponse>> searchTickets(
			@Parameter(description = "Search query", example = "internet") @RequestParam("q") String query
	) {
		return ApiResponse.success("Ticket search completed successfully", searchService.searchTickets(query));
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Search users", description = "Runs fuzzy and partial search against user first name, last name, and email.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User search completed successfully")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
	public ApiResponse<List<UserResponse>> searchUsers(
			@Parameter(description = "Search query", example = "john") @RequestParam("q") String query
	) {
		return ApiResponse.success("User search completed successfully", searchService.searchUsers(query));
	}
}
