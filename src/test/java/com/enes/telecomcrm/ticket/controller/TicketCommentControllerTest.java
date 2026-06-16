package com.enes.telecomcrm.ticket.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.common.exception.GlobalExceptionHandler;
import com.enes.telecomcrm.ticket.dto.TicketCommentRequest;
import com.enes.telecomcrm.ticket.dto.TicketCommentResponse;
import com.enes.telecomcrm.ticket.exception.TicketNotFoundException;
import com.enes.telecomcrm.ticket.service.TicketCommentService;
import com.enes.telecomcrm.user.dto.UserResponse;

class TicketCommentControllerTest {

	private static final String COMMENT_REQUEST = """
			{
			  "message": "Looking into this."
			}
			""";

	private final TicketCommentService ticketCommentService = mock(TicketCommentService.class);
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TicketCommentController(ticketCommentService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void addComment_returnsTicketCommentResponseDto() throws Exception {
		TicketCommentRequest request = new TicketCommentRequest("Looking into this.");
		when(ticketCommentService.addComment(10L, request)).thenReturn(response(100L));

		mockMvc.perform(post("/api/v1/tickets/10/comments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(COMMENT_REQUEST))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Ticket comment added successfully"))
				.andExpect(jsonPath("$.data.id").value(100))
				.andExpect(jsonPath("$.data.message").value("Looking into this."))
				.andExpect(jsonPath("$.data.ticketId").value(10))
				.andExpect(jsonPath("$.data.author.id").value(1));
	}

	@Test
	void getComments_returnsTicketCommentResponseDtos() throws Exception {
		when(ticketCommentService.getComments(10L)).thenReturn(List.of(response(100L), response(101L)));

		mockMvc.perform(get("/api/v1/tickets/10/comments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Ticket comments retrieved successfully"))
				.andExpect(jsonPath("$.data[0].id").value(100))
				.andExpect(jsonPath("$.data[1].id").value(101));
	}

	@Test
	void addComment_whenRequestInvalidReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/v1/tickets/10/comments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void addComment_whenTicketClosedReturnsUnprocessableEntity() throws Exception {
		when(ticketCommentService.addComment(10L, new TicketCommentRequest("Looking into this.")))
				.thenThrow(new BusinessRuleException("Comments are not allowed on closed tickets"));

		mockMvc.perform(post("/api/v1/tickets/10/comments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(COMMENT_REQUEST))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Comments are not allowed on closed tickets"));
	}

	@Test
	void getComments_whenTicketMissingReturnsNotFound() throws Exception {
		when(ticketCommentService.getComments(99L)).thenThrow(new TicketNotFoundException(99L));

		mockMvc.perform(get("/api/v1/tickets/99/comments"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Ticket not found with id: 99"));
	}

	private TicketCommentResponse response(Long id) {
		return new TicketCommentResponse(
				id,
				"Looking into this.",
				new UserResponse(1L, "John", "Doe", "user1@example.com", "ROLE_USER", null, null),
				10L,
				LocalDateTime.now()
		);
	}
}
