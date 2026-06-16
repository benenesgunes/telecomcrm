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

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

	private final SearchService searchService;

	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}

	@GetMapping("/tickets")
	@PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
	public ApiResponse<List<TicketResponse>> searchTickets(@RequestParam("q") String query) {
		return ApiResponse.success("Ticket search completed successfully", searchService.searchTickets(query));
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<List<UserResponse>> searchUsers(@RequestParam("q") String query) {
		return ApiResponse.success("User search completed successfully", searchService.searchUsers(query));
	}
}
