package com.enes.telecomcrm.ticket.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.common.exception.GlobalExceptionHandler;
import com.enes.telecomcrm.ticket.dto.TicketAssignRequest;
import com.enes.telecomcrm.ticket.dto.TicketRequest;
import com.enes.telecomcrm.ticket.dto.TicketResponse;
import com.enes.telecomcrm.ticket.entity.TicketPriority;
import com.enes.telecomcrm.ticket.exception.TicketNotFoundException;
import com.enes.telecomcrm.ticket.service.TicketService;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

class TicketControllerTest {

	private static final String TICKET_REQUEST = """
			{
			  "title": "Internet drops",
			  "description": "Connection drops every morning.",
			  "priority": "HIGH"
			}
			""";
	private static final String ASSIGN_REQUEST = """
			{
			  "agentId": 2
			}
			""";

	private final TicketService ticketService = mock(TicketService.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TicketController(ticketService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void createTicket_returnsTicketResponseDto() throws Exception {
		TicketRequest request = new TicketRequest("Internet drops", "Connection drops every morning.", TicketPriority.HIGH);
		when(ticketService.createTicket(request)).thenReturn(response(10L, "OPEN"));

		mockMvc.perform(post("/api/v1/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(TICKET_REQUEST))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Ticket created successfully"))
				.andExpect(jsonPath("$.data.id").value(10))
				.andExpect(jsonPath("$.data.status").value("OPEN"));
	}

	@Test
	void getAllTickets_returnsTicketResponseDtos() throws Exception {
		when(ticketService.getAllTickets()).thenReturn(List.of(response(10L, "OPEN"), response(11L, "RESOLVED")));

		mockMvc.perform(get("/api/v1/tickets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Tickets retrieved successfully"))
				.andExpect(jsonPath("$.data[0].id").value(10))
				.andExpect(jsonPath("$.data[1].status").value("RESOLVED"));
	}

	@Test
	void getMyTickets_returnsTicketResponseDtos() throws Exception {
		when(ticketService.getMyTickets()).thenReturn(List.of(response(10L, "OPEN")));

		mockMvc.perform(get("/api/v1/tickets/my"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User tickets retrieved successfully"))
				.andExpect(jsonPath("$.data[0].id").value(10));
	}

	@Test
	void getTicketById_returnsTicketResponseDto() throws Exception {
		when(ticketService.getTicketById(10L)).thenReturn(response(10L, "OPEN"));

		mockMvc.perform(get("/api/v1/tickets/10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Ticket retrieved successfully"))
				.andExpect(jsonPath("$.data.id").value(10));
	}

	@Test
	void assignTicket_returnsAssignedTicketResponseDto() throws Exception {
		TicketAssignRequest request = new TicketAssignRequest(2L);
		when(ticketService.assignTicket(10L, request)).thenReturn(response(10L, "IN_PROGRESS"));

		mockMvc.perform(patch("/api/v1/tickets/10/assign")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Ticket assigned successfully"))
				.andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
	}

	@Test
	void resolveTicket_returnsResolvedTicketResponseDto() throws Exception {
		when(ticketService.resolveTicket(10L)).thenReturn(response(10L, "RESOLVED"));

		mockMvc.perform(patch("/api/v1/tickets/10/resolve"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Ticket resolved successfully"))
				.andExpect(jsonPath("$.data.status").value("RESOLVED"));
	}

	@Test
	void closeTicket_returnsClosedTicketResponseDto() throws Exception {
		when(ticketService.closeTicket(10L)).thenReturn(response(10L, "CLOSED"));

		mockMvc.perform(patch("/api/v1/tickets/10/close"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Ticket closed successfully"))
				.andExpect(jsonPath("$.data.status").value("CLOSED"));
	}

	@Test
	void createTicket_whenRequestInvalidReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/v1/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void getTicketById_whenMissingReturnsNotFound() throws Exception {
		when(ticketService.getTicketById(99L)).thenThrow(new TicketNotFoundException(99L));

		mockMvc.perform(get("/api/v1/tickets/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Ticket not found with id: 99"));
	}

	@Test
	void assignTicket_whenBusinessRuleFailsReturnsUnprocessableEntity() throws Exception {
		when(ticketService.assignTicket(10L, new TicketAssignRequest(2L)))
				.thenThrow(new BusinessRuleException("Only open tickets are assignable"));

		mockMvc.perform(patch("/api/v1/tickets/10/assign")
						.contentType(MediaType.APPLICATION_JSON)
						.content(ASSIGN_REQUEST))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Only open tickets are assignable"));
	}

	private TicketResponse response(Long id, String status) {
		return new TicketResponse(
				id,
				"Internet drops",
				"Connection drops every morning.",
				status,
				"HIGH",
				new UserResponse(1L, "John", "Doe", "user1@example.com", "ROLE_USER", null, null),
				null,
				null,
				null
		);
	}
}
