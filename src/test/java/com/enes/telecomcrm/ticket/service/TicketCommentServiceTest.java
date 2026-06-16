package com.enes.telecomcrm.ticket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.enes.telecomcrm.auth.security.UserPrincipal;
import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.ticket.dto.TicketCommentRequest;
import com.enes.telecomcrm.ticket.dto.TicketCommentResponse;
import com.enes.telecomcrm.ticket.entity.Ticket;
import com.enes.telecomcrm.ticket.entity.TicketComment;
import com.enes.telecomcrm.ticket.entity.TicketPriority;
import com.enes.telecomcrm.ticket.entity.TicketStatus;
import com.enes.telecomcrm.ticket.exception.TicketNotFoundException;
import com.enes.telecomcrm.ticket.mapper.TicketCommentMapper;
import com.enes.telecomcrm.ticket.repository.TicketCommentRepository;
import com.enes.telecomcrm.ticket.repository.TicketRepository;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.exception.UserNotFoundException;

import jakarta.persistence.EntityManager;

class TicketCommentServiceTest {

	private final TicketRepository ticketRepository = mock(TicketRepository.class);
	private final TicketCommentRepository ticketCommentRepository = mock(TicketCommentRepository.class);
	private final TicketCommentMapper ticketCommentMapper = mock(TicketCommentMapper.class);
	private final EntityManager entityManager = mock(EntityManager.class);
	private final TicketCommentService ticketCommentService = new TicketCommentService(
			ticketRepository,
			ticketCommentRepository,
			ticketCommentMapper,
			entityManager
	);

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void addComment_whenTicketOpenSavesCommentWithAuthorAndTicket() {
		authenticate(1L, Role.ROLE_USER);
		User author = user(1L, Role.ROLE_USER);
		Ticket ticket = ticket(10L, author, null, TicketStatus.OPEN);
		TicketCommentRequest request = new TicketCommentRequest("Looking into this.");
		TicketComment mappedComment = TicketComment.builder().message("Looking into this.").build();
		TicketComment savedComment = comment(100L, ticket, author);
		TicketCommentResponse response = response(100L);

		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(entityManager.find(User.class, 1L)).thenReturn(author);
		when(ticketCommentMapper.toEntity(request)).thenReturn(mappedComment);
		when(ticketCommentRepository.save(mappedComment)).thenReturn(savedComment);
		when(ticketCommentMapper.toResponse(savedComment)).thenReturn(response);

		assertEquals(response, ticketCommentService.addComment(10L, request));

		ArgumentCaptor<TicketComment> commentCaptor = ArgumentCaptor.forClass(TicketComment.class);
		verify(ticketCommentRepository).save(commentCaptor.capture());
		assertEquals(ticket, commentCaptor.getValue().getTicket());
		assertEquals(author, commentCaptor.getValue().getAuthor());
	}

	@Test
	void addComment_whenTicketClosedThrowsBusinessRuleException() {
		authenticate(1L, Role.ROLE_USER);
		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(10L, user(1L, Role.ROLE_USER), null, TicketStatus.CLOSED)));

		BusinessRuleException exception = assertThrows(
				BusinessRuleException.class,
				() -> ticketCommentService.addComment(10L, new TicketCommentRequest("Looking into this."))
		);

		assertEquals("Comments are not allowed on closed tickets", exception.getMessage());
	}

	@Test
	void addComment_whenTicketMissingThrowsTicketNotFoundException() {
		authenticate(1L, Role.ROLE_USER);
		when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(TicketNotFoundException.class, () -> ticketCommentService.addComment(99L, new TicketCommentRequest("Message")));
	}

	@Test
	void addComment_whenCurrentUserMissingThrowsUserNotFoundException() {
		authenticate(1L, Role.ROLE_USER);
		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(10L, user(2L, Role.ROLE_USER), null, TicketStatus.OPEN)));
		when(entityManager.find(User.class, 1L)).thenReturn(null);

		assertThrows(UserNotFoundException.class, () -> ticketCommentService.addComment(10L, new TicketCommentRequest("Message")));
	}

	@Test
	void getComments_whenTicketExistsReturnsOrderedMappedComments() {
		Ticket ticket = ticket(10L, user(1L, Role.ROLE_USER), null, TicketStatus.OPEN);
		List<TicketComment> comments = List.of(comment(100L, ticket, ticket.getCustomer()));
		List<TicketCommentResponse> responses = List.of(response(100L));

		when(ticketRepository.existsById(10L)).thenReturn(true);
		when(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(10L)).thenReturn(comments);
		when(ticketCommentMapper.toResponseList(comments)).thenReturn(responses);

		assertEquals(responses, ticketCommentService.getComments(10L));
	}

	@Test
	void getComments_whenTicketMissingThrowsTicketNotFoundException() {
		when(ticketRepository.existsById(99L)).thenReturn(false);

		assertThrows(TicketNotFoundException.class, () -> ticketCommentService.getComments(99L));
	}

	private void authenticate(Long id, Role role) {
		UserPrincipal principal = new UserPrincipal(id, "user%d@example.com".formatted(id), "password", role);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				principal,
				null,
				principal.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
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

	private Ticket ticket(Long id, User customer, User assignedAgent, TicketStatus status) {
		return Ticket.builder()
				.id(id)
				.title("Internet drops")
				.description("Connection drops every morning.")
				.priority(TicketPriority.HIGH)
				.status(status)
				.customer(customer)
				.assignedAgent(assignedAgent)
				.build();
	}

	private TicketComment comment(Long id, Ticket ticket, User author) {
		return TicketComment.builder()
				.id(id)
				.message("Looking into this.")
				.ticket(ticket)
				.author(author)
				.createdAt(LocalDateTime.now())
				.build();
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
