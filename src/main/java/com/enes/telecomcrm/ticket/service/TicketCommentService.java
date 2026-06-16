package com.enes.telecomcrm.ticket.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.common.exception.UnauthorizedException;
import com.enes.telecomcrm.common.util.SecurityUtils;
import com.enes.telecomcrm.ticket.dto.TicketCommentRequest;
import com.enes.telecomcrm.ticket.dto.TicketCommentResponse;
import com.enes.telecomcrm.ticket.entity.Ticket;
import com.enes.telecomcrm.ticket.entity.TicketComment;
import com.enes.telecomcrm.ticket.entity.TicketStatus;
import com.enes.telecomcrm.ticket.exception.TicketNotFoundException;
import com.enes.telecomcrm.ticket.mapper.TicketCommentMapper;
import com.enes.telecomcrm.ticket.repository.TicketCommentRepository;
import com.enes.telecomcrm.ticket.repository.TicketRepository;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.exception.UserNotFoundException;

import jakarta.persistence.EntityManager;

@Service
public class TicketCommentService {

	private final TicketRepository ticketRepository;
	private final TicketCommentRepository ticketCommentRepository;
	private final TicketCommentMapper ticketCommentMapper;
	private final EntityManager entityManager;

	public TicketCommentService(
			TicketRepository ticketRepository,
			TicketCommentRepository ticketCommentRepository,
			TicketCommentMapper ticketCommentMapper,
			EntityManager entityManager
	) {
		this.ticketRepository = ticketRepository;
		this.ticketCommentRepository = ticketCommentRepository;
		this.ticketCommentMapper = ticketCommentMapper;
		this.entityManager = entityManager;
	}

	@Transactional
	public TicketCommentResponse addComment(Long ticketId, TicketCommentRequest request) {
		Ticket ticket = findTicketById(ticketId);
		if (ticket.getStatus() == TicketStatus.CLOSED) {
			throw new BusinessRuleException("Comments are not allowed on closed tickets");
		}

		User author = findUserById(currentUserId());
		TicketComment ticketComment = ticketCommentMapper.toEntity(request);
		ticketComment.setTicket(ticket);
		ticketComment.setAuthor(author);

		return ticketCommentMapper.toResponse(ticketCommentRepository.save(ticketComment));
	}

	@Transactional(readOnly = true)
	public List<TicketCommentResponse> getComments(Long ticketId) {
		if (!ticketRepository.existsById(ticketId)) {
			throw new TicketNotFoundException(ticketId);
		}
		return ticketCommentMapper.toResponseList(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId));
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
}
