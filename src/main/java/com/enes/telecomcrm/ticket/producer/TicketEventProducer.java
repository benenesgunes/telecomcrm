package com.enes.telecomcrm.ticket.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.enes.telecomcrm.ticket.entity.Ticket;
import com.enes.telecomcrm.ticket.event.TicketCreatedEvent;
import com.enes.telecomcrm.ticket.event.TicketCreatedPayload;
import com.enes.telecomcrm.ticket.event.TicketResolvedEvent;
import com.enes.telecomcrm.ticket.event.TicketResolvedPayload;

@Component
public class TicketEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final String ticketCreatedTopic;
	private final String ticketResolvedTopic;

	public TicketEventProducer(
			KafkaTemplate<String, Object> kafkaTemplate,
			@Value("${app.kafka.topics.ticket-created:ticket-created}") String ticketCreatedTopic,
			@Value("${app.kafka.topics.ticket-resolved:ticket-resolved}") String ticketResolvedTopic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.ticketCreatedTopic = ticketCreatedTopic;
		this.ticketResolvedTopic = ticketResolvedTopic;
	}

	public void publishTicketCreated(Ticket ticket) {
		TicketCreatedPayload payload = new TicketCreatedPayload(
				ticket.getId(),
				ticket.getTitle(),
				ticket.getPriority().name(),
				ticket.getCustomer().getId(),
				ticket.getCustomer().getEmail()
		);
		kafkaTemplate.send(ticketCreatedTopic, String.valueOf(ticket.getId()), TicketCreatedEvent.of(payload));
	}

	public void publishTicketResolved(Ticket ticket, long resolutionTimeMinutes) {
		TicketResolvedPayload payload = new TicketResolvedPayload(
				ticket.getId(),
				ticket.getAssignedAgent() == null ? null : ticket.getAssignedAgent().getId(),
				resolutionTimeMinutes,
				ticket.getCustomer().getId(),
				ticket.getCustomer().getEmail()
		);
		kafkaTemplate.send(ticketResolvedTopic, String.valueOf(ticket.getId()), TicketResolvedEvent.of(payload));
	}
}
