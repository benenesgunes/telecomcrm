package com.enes.telecomcrm.ticket.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import com.enes.telecomcrm.ticket.entity.Ticket;
import com.enes.telecomcrm.ticket.entity.TicketPriority;
import com.enes.telecomcrm.ticket.entity.TicketStatus;
import com.enes.telecomcrm.ticket.event.TicketCreatedEvent;
import com.enes.telecomcrm.ticket.event.TicketResolvedEvent;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;

class TicketEventProducerTest {

	private final KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
	private final TicketEventProducer ticketEventProducer = new TicketEventProducer(
			kafkaTemplate,
			"ticket-created",
			"ticket-resolved",
			true
	);

	@Test
	void publishTicketCreated_sendsTicketCreatedEventToConfiguredTopic() {
		Ticket ticket = ticket(101L, user(5L, Role.ROLE_USER), null);
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

		ticketEventProducer.publishTicketCreated(ticket);

		verify(kafkaTemplate).send(eq("ticket-created"), eq("101"), eventCaptor.capture());
		TicketCreatedEvent event = (TicketCreatedEvent) eventCaptor.getValue();
		assertNotNull(event.eventId());
		assertEquals("TICKET_CREATED", event.eventType());
		assertEquals(101L, event.payload().ticketId());
		assertEquals("Internet drops every morning", event.payload().title());
		assertEquals("HIGH", event.payload().priority());
		assertEquals(5L, event.payload().customerId());
		assertEquals("user5@example.com", event.payload().customerEmail());
	}

	@Test
	void publishTicketResolved_sendsTicketResolvedEventToConfiguredTopic() {
		Ticket ticket = ticket(101L, user(5L, Role.ROLE_USER), user(12L, Role.ROLE_SUPPORT_AGENT));
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

		ticketEventProducer.publishTicketResolved(ticket, 1410L);

		verify(kafkaTemplate).send(eq("ticket-resolved"), eq("101"), eventCaptor.capture());
		TicketResolvedEvent event = (TicketResolvedEvent) eventCaptor.getValue();
		assertNotNull(event.eventId());
		assertEquals("TICKET_RESOLVED", event.eventType());
		assertEquals(101L, event.payload().ticketId());
		assertEquals(12L, event.payload().resolvedByAgentId());
		assertEquals(1410L, event.payload().resolutionTimeMinutes());
		assertEquals(5L, event.payload().customerId());
		assertEquals("user5@example.com", event.payload().customerEmail());
	}

	@Test
	void publishTicketCreated_whenKafkaDisabledDoesNotSendEvent() {
		TicketEventProducer disabledProducer = new TicketEventProducer(
				kafkaTemplate,
				"ticket-created",
				"ticket-resolved",
				false
		);

		disabledProducer.publishTicketCreated(ticket(101L, user(5L, Role.ROLE_USER), null));

		verify(kafkaTemplate, never()).send(eq("ticket-created"), eq("101"), org.mockito.ArgumentMatchers.any());
	}

	private Ticket ticket(Long id, User customer, User assignedAgent) {
		return Ticket.builder()
				.id(id)
				.title("Internet drops every morning")
				.description("Connection drops every morning.")
				.priority(TicketPriority.HIGH)
				.status(TicketStatus.OPEN)
				.customer(customer)
				.assignedAgent(assignedAgent)
				.build();
	}

	private User user(Long id, Role role) {
		return User.builder()
				.id(id)
				.firstName("John")
				.lastName("Doe")
				.email("user%d@example.com".formatted(id))
				.password("hashed-password")
				.role(role)
				.build();
	}
}
