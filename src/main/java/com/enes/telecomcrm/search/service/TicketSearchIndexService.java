package com.enes.telecomcrm.search.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.enes.telecomcrm.search.document.TicketDocument;
import com.enes.telecomcrm.search.repository.TicketSearchRepository;
import com.enes.telecomcrm.ticket.entity.Ticket;

@Service
public class TicketSearchIndexService {

	private static final Logger log = LoggerFactory.getLogger(TicketSearchIndexService.class);

	private final TicketSearchRepository ticketSearchRepository;

	public TicketSearchIndexService(TicketSearchRepository ticketSearchRepository) {
		this.ticketSearchRepository = ticketSearchRepository;
	}

	public void index(Ticket ticket) {
		try {
			ticketSearchRepository.save(toDocument(ticket));
		}
		catch (RuntimeException ex) {
			log.warn("Failed to index ticket id={}", ticket.getId(), ex);
		}
	}

	private TicketDocument toDocument(Ticket ticket) {
		return TicketDocument.builder()
				.id(String.valueOf(ticket.getId()))
				.title(ticket.getTitle())
				.description(ticket.getDescription())
				.status(ticket.getStatus().name())
				.priority(ticket.getPriority().name())
				.customerId(ticket.getCustomer().getId())
				.customerEmail(ticket.getCustomer().getEmail())
				.assignedAgentId(ticket.getAssignedAgent() == null ? null : ticket.getAssignedAgent().getId())
				.createdAt(ticket.getCreatedAt())
				.updatedAt(ticket.getUpdatedAt())
				.build();
	}
}
