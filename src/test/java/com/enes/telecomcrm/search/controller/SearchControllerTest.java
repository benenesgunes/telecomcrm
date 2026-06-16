package com.enes.telecomcrm.search.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.enes.telecomcrm.common.exception.GlobalExceptionHandler;
import com.enes.telecomcrm.search.service.SearchService;
import com.enes.telecomcrm.ticket.dto.TicketResponse;
import com.enes.telecomcrm.user.dto.UserResponse;

class SearchControllerTest {

	private final SearchService searchService = mock(SearchService.class);
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new SearchController(searchService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void searchTickets_returnsTicketSearchResults() throws Exception {
		UserResponse customer = new UserResponse(1L, null, null, "john@example.com", null, null, null);
		TicketResponse ticket = new TicketResponse(
				10L,
				"Internet drops",
				"Connection drops every morning.",
				"OPEN",
				"HIGH",
				customer,
				null,
				null,
				null
		);

		when(searchService.searchTickets("internet")).thenReturn(List.of(ticket));

		mockMvc.perform(get("/api/v1/search/tickets").param("q", "internet"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Ticket search completed successfully"))
				.andExpect(jsonPath("$.data[0].id").value(10))
				.andExpect(jsonPath("$.data[0].title").value("Internet drops"));
	}

	@Test
	void searchUsers_returnsUserSearchResults() throws Exception {
		UserResponse user = new UserResponse(1L, "John", "Doe", "john@example.com", "ROLE_USER", null, null);

		when(searchService.searchUsers("john")).thenReturn(List.of(user));

		mockMvc.perform(get("/api/v1/search/users").param("q", "john"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User search completed successfully"))
				.andExpect(jsonPath("$.data[0].id").value(1))
				.andExpect(jsonPath("$.data[0].email").value("john@example.com"));
	}
}
