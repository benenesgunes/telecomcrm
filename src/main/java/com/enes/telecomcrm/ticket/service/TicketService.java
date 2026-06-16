package com.enes.telecomcrm.ticket.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.common.exception.UnauthorizedException;
import com.enes.telecomcrm.common.util.SecurityUtils;
import com.enes.telecomcrm.subscription.entity.SubscriptionStatus;
import com.enes.telecomcrm.subscription.repository.SubscriptionRepository;
import com.enes.telecomcrm.ticket.dto.TicketAssignRequest;
import com.enes.telecomcrm.ticket.dto.TicketRequest;
import com.enes.telecomcrm.ticket.dto.TicketResponse;
import com.enes.telecomcrm.ticket.entity.Ticket;
import com.enes.telecomcrm.ticket.entity.TicketStatus;
import com.enes.telecomcrm.ticket.exception.TicketNotFoundException;
import com.enes.telecomcrm.ticket.mapper.TicketMapper;
import com.enes.telecomcrm.ticket.producer.TicketEventProducer;
import com.enes.telecomcrm.ticket.repository.TicketRepository;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.exception.UserNotFoundException;

import jakarta.persistence.EntityManager;

@Service
public class TicketService {

	private final TicketRepository ticketRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final TicketMapper ticketMapper;
	private final EntityManager entityManager;
	private final TicketEventProducer ticketEventProducer;

	public TicketService(
			TicketRepository ticketRepository,
			SubscriptionRepository subscriptionRepository,
			TicketMapper ticketMapper,
			EntityManager entityManager,
			TicketEventProducer ticketEventProducer
	) {
		this.ticketRepository = ticketRepository;
		this.subscriptionRepository = subscriptionRepository;
		this.ticketMapper = ticketMapper;
		this.entityManager = entityManager;
		this.ticketEventProducer = ticketEventProducer;
	}

	@Transactional
	@CacheEvict(cacheNames = {"dashboard:admin", "dashboard:tickets"}, allEntries = true)
	public TicketResponse createTicket(TicketRequest request) {
		Long customerId = currentUserId();
		User customer = findUserById(customerId);

		if (!subscriptionRepository.existsByUserIdAndStatus(customerId, SubscriptionStatus.ACTIVE)) {
			throw new BusinessRuleException("Active subscription required to create a ticket");
		}

		Ticket ticket = ticketMapper.toEntity(request);
		ticket.setCustomer(customer);
		ticket.setAssignedAgent(null);
		ticket.setStatus(TicketStatus.OPEN);

		Ticket savedTicket = ticketRepository.save(ticket);
		ticketEventProducer.publishTicketCreated(savedTicket);
		return ticketMapper.toResponse(savedTicket);
	}

	@Transactional(readOnly = true)
	public List<TicketResponse> getAllTickets() {
		return ticketMapper.toResponseList(ticketRepository.findAll());
	}

	@Transactional(readOnly = true)
	public List<TicketResponse> getMyTickets() {
		return ticketMapper.toResponseList(ticketRepository.findByCustomerId(currentUserId()));
	}

	@Transactional(readOnly = true)
	public TicketResponse getTicketById(Long id) {
		return ticketMapper.toResponse(findTicketById(id));
	}

	@Transactional
	@CacheEvict(cacheNames = "dashboard:agent", key = "#request.agentId()")
	public TicketResponse assignTicket(Long id, TicketAssignRequest request) {
		Ticket ticket = findTicketById(id);
		ensureNotClosed(ticket);

		if (ticket.getStatus() != TicketStatus.OPEN) {
			throw new BusinessRuleException("Only open tickets are assignable");
		}

		User agent = findUserById(request.agentId());
		if (agent.getRole() != Role.ROLE_SUPPORT_AGENT && agent.getRole() != Role.ROLE_ADMIN) {
			throw new BusinessRuleException("Ticket can only be assigned to a support agent or admin");
		}

		ticket.setAssignedAgent(agent);
		ticket.setStatus(TicketStatus.IN_PROGRESS);

		return ticketMapper.toResponse(ticketRepository.save(ticket));
	}

	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = {"dashboard:admin", "dashboard:tickets"}, allEntries = true),
			@CacheEvict(
					cacheNames = "dashboard:agent",
					key = "#result.assignedAgent().id()",
					condition = "#result.assignedAgent() != null"
			)
	})
	public TicketResponse resolveTicket(Long id) {
		Ticket ticket = findTicketById(id);
		ensureNotClosed(ticket);

		if (ticket.getStatus() == TicketStatus.RESOLVED) {
			return ticketMapper.toResponse(ticket);
		}

		ticket.setStatus(TicketStatus.RESOLVED);

		Ticket savedTicket = ticketRepository.save(ticket);
		ticketEventProducer.publishTicketResolved(savedTicket, resolutionTimeMinutes(savedTicket));
		return ticketMapper.toResponse(savedTicket);
	}

	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = {"dashboard:admin", "dashboard:tickets"}, allEntries = true),
			@CacheEvict(
					cacheNames = "dashboard:agent",
					key = "#result.assignedAgent().id()",
					condition = "#result.assignedAgent() != null"
			)
	})
	public TicketResponse closeTicket(Long id) {
		Ticket ticket = findTicketById(id);
		ensureNotClosed(ticket);

		ticket.setStatus(TicketStatus.CLOSED);
		return ticketMapper.toResponse(ticketRepository.save(ticket));
	}

	private void ensureNotClosed(Ticket ticket) {
		if (ticket.getStatus() == TicketStatus.CLOSED) {
			throw new BusinessRuleException("Closed ticket cannot be modified");
		}
	}

	private Ticket findTicketById(Long id) {
		return ticketRepository.findById(id)
				.orElseThrow(() -> new TicketNotFoundException(id));
	}

	private User findUserById(Long id) {
		User user = entityManager.find(User.class, id);
		if (user == null) {
			throw new UserNotFoundException(id);
		}
		return user;
	}

	private Long currentUserId() {
		return SecurityUtils.getCurrentUserId()
				.orElseThrow(() -> new UnauthorizedException("Authenticated user is required"));
	}

	private long resolutionTimeMinutes(Ticket ticket) {
		LocalDateTime createdAt = ticket.getCreatedAt();
		if (createdAt == null) {
			return 0;
		}
		return ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
	}
}
