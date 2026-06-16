package com.enes.telecomcrm.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.enes.telecomcrm.search.document.TicketDocument;
import com.enes.telecomcrm.search.repository.TicketSearchRepository;
import com.enes.telecomcrm.ticket.entity.Ticket;
import com.enes.telecomcrm.ticket.entity.TicketPriority;
import com.enes.telecomcrm.ticket.entity.TicketStatus;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;

class TicketSearchIndexServiceTest {

	private final TicketSearchRepository ticketSearchRepository = mock(TicketSearchRepository.class);
	private final TicketSearchIndexService ticketSearchIndexService = new TicketSearchIndexService(ticketSearchRepository);

	@Test
	void index_savesTicketDocumentWithSearchFields() {
		LocalDateTime now = LocalDateTime.now();
		User customer = user(1L, "customer@example.com", Role.ROLE_USER);
		User agent = user(2L, "agent@example.com", Role.ROLE_SUPPORT_AGENT);
		Ticket ticket = Ticket.builder()
				.id(10L)
				.title("Internet drops")
				.description("Connection drops every morning.")
				.status(TicketStatus.IN_PROGRESS)
				.priority(TicketPriority.HIGH)
				.customer(customer)
				.assignedAgent(agent)
				.createdAt(now)
				.updatedAt(now)
				.build();

		ticketSearchIndexService.index(ticket);

		ArgumentCaptor<TicketDocument> documentCaptor = ArgumentCaptor.forClass(TicketDocument.class);
		verify(ticketSearchRepository).save(documentCaptor.capture());
		TicketDocument document = documentCaptor.getValue();
		assertThat(document.getId()).isEqualTo("10");
		assertThat(document.getTitle()).isEqualTo("Internet drops");
		assertThat(document.getDescription()).isEqualTo("Connection drops every morning.");
		assertThat(document.getStatus()).isEqualTo("IN_PROGRESS");
		assertThat(document.getPriority()).isEqualTo("HIGH");
		assertThat(document.getCustomerId()).isEqualTo(1L);
		assertThat(document.getCustomerEmail()).isEqualTo("customer@example.com");
		assertThat(document.getAssignedAgentId()).isEqualTo(2L);
		assertThat(document.getCreatedAt()).isEqualTo(now);
		assertThat(document.getUpdatedAt()).isEqualTo(now);
	}

	@Test
	void index_whenElasticsearchFailsDoesNotThrow() {
		Ticket ticket = Ticket.builder()
				.id(10L)
				.title("Internet drops")
				.description("Connection drops every morning.")
				.status(TicketStatus.OPEN)
				.priority(TicketPriority.MEDIUM)
				.customer(user(1L, "customer@example.com", Role.ROLE_USER))
				.build();
		when(ticketSearchRepository.save(any(TicketDocument.class))).thenThrow(new RuntimeException("Elasticsearch down"));

		assertDoesNotThrow(() -> ticketSearchIndexService.index(ticket));
	}

	private User user(Long id, String email, Role role) {
		return User.builder()
				.id(id)
				.firstName("John")
				.lastName("Doe")
				.email(email)
				.password("secret")
				.role(role)
				.build();
	}
}
