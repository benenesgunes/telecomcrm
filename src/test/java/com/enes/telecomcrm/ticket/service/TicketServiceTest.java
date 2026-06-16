package com.enes.telecomcrm.ticket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import com.enes.telecomcrm.common.exception.UnauthorizedException;
import com.enes.telecomcrm.search.service.TicketSearchIndexService;
import com.enes.telecomcrm.subscription.entity.SubscriptionStatus;
import com.enes.telecomcrm.subscription.repository.SubscriptionRepository;
import com.enes.telecomcrm.ticket.dto.TicketAssignRequest;
import com.enes.telecomcrm.ticket.dto.TicketRequest;
import com.enes.telecomcrm.ticket.dto.TicketResponse;
import com.enes.telecomcrm.ticket.entity.Ticket;
import com.enes.telecomcrm.ticket.entity.TicketPriority;
import com.enes.telecomcrm.ticket.entity.TicketStatus;
import com.enes.telecomcrm.ticket.exception.TicketNotFoundException;
import com.enes.telecomcrm.ticket.mapper.TicketMapper;
import com.enes.telecomcrm.ticket.producer.TicketEventProducer;
import com.enes.telecomcrm.ticket.repository.TicketRepository;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.exception.UserNotFoundException;

import jakarta.persistence.EntityManager;

class TicketServiceTest {

	private final TicketRepository ticketRepository = mock(TicketRepository.class);
	private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
	private final TicketMapper ticketMapper = mock(TicketMapper.class);
	private final EntityManager entityManager = mock(EntityManager.class);
	private final TicketEventProducer ticketEventProducer = mock(TicketEventProducer.class);
	private final TicketSearchIndexService ticketSearchIndexService = mock(TicketSearchIndexService.class);
	private final TicketService ticketService = new TicketService(
			ticketRepository,
			subscriptionRepository,
			ticketMapper,
			entityManager,
			ticketEventProducer,
			ticketSearchIndexService
	);

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void createTicket_whenUserHasActiveSubscriptionCreatesOpenTicketAndPublishesEvent() {
		authenticate(1L, Role.ROLE_USER);
		User customer = user(1L, Role.ROLE_USER);
		TicketRequest request = new TicketRequest("Internet drops", "Connection drops every morning.", TicketPriority.HIGH);
		Ticket mappedTicket = ticket(null, customer, null, null);
		Ticket savedTicket = ticket(10L, customer, null, TicketStatus.OPEN);
		TicketResponse response = response(10L, "OPEN");

		when(entityManager.find(User.class, 1L)).thenReturn(customer);
		when(subscriptionRepository.existsByUserIdAndStatus(1L, SubscriptionStatus.ACTIVE)).thenReturn(true);
		when(ticketMapper.toEntity(request)).thenReturn(mappedTicket);
		when(ticketRepository.save(mappedTicket)).thenReturn(savedTicket);
		when(ticketMapper.toResponse(savedTicket)).thenReturn(response);

		assertEquals(response, ticketService.createTicket(request));
		assertEquals(customer, mappedTicket.getCustomer());
		assertNull(mappedTicket.getAssignedAgent());
		assertEquals(TicketStatus.OPEN, mappedTicket.getStatus());
		verify(ticketSearchIndexService).index(savedTicket);
		verify(ticketEventProducer).publishTicketCreated(savedTicket);
	}

	@Test
	void createTicket_whenUserHasNoActiveSubscriptionThrowsBusinessRuleException() {
		authenticate(1L, Role.ROLE_USER);
		when(entityManager.find(User.class, 1L)).thenReturn(user(1L, Role.ROLE_USER));
		when(subscriptionRepository.existsByUserIdAndStatus(1L, SubscriptionStatus.ACTIVE)).thenReturn(false);

		TicketRequest request = new TicketRequest("Internet drops", "Connection drops every morning.", TicketPriority.HIGH);

		assertThrows(BusinessRuleException.class, () -> ticketService.createTicket(request));
	}

	@Test
	void createTicket_whenCurrentUserDoesNotExistThrowsUserNotFoundException() {
		authenticate(1L, Role.ROLE_USER);
		when(entityManager.find(User.class, 1L)).thenReturn(null);

		TicketRequest request = new TicketRequest("Internet drops", "Connection drops every morning.", TicketPriority.HIGH);

		assertThrows(UserNotFoundException.class, () -> ticketService.createTicket(request));
	}

	@Test
	void createTicket_whenUserIsNotAuthenticatedThrowsUnauthorizedException() {
		TicketRequest request = new TicketRequest("Internet drops", "Connection drops every morning.", TicketPriority.HIGH);

		assertThrows(UnauthorizedException.class, () -> ticketService.createTicket(request));
	}

	@Test
	void getAllTickets_returnsMappedResponses() {
		List<Ticket> tickets = List.of(ticket(10L, user(1L, Role.ROLE_USER), null, TicketStatus.OPEN));
		List<TicketResponse> responses = List.of(response(10L, "OPEN"));

		when(ticketRepository.findAll()).thenReturn(tickets);
		when(ticketMapper.toResponseList(tickets)).thenReturn(responses);

		assertEquals(responses, ticketService.getAllTickets());
	}

	@Test
	void getMyTickets_returnsCurrentUsersTickets() {
		authenticate(1L, Role.ROLE_USER);
		List<Ticket> tickets = List.of(ticket(10L, user(1L, Role.ROLE_USER), null, TicketStatus.OPEN));
		List<TicketResponse> responses = List.of(response(10L, "OPEN"));

		when(ticketRepository.findByCustomerId(1L)).thenReturn(tickets);
		when(ticketMapper.toResponseList(tickets)).thenReturn(responses);

		assertEquals(responses, ticketService.getMyTickets());
	}

	@Test
	void getMyTickets_whenUserIsNotAuthenticatedThrowsUnauthorizedException() {
		assertThrows(UnauthorizedException.class, ticketService::getMyTickets);
	}

	@Test
	void getTicketById_whenExistsReturnsMappedResponse() {
		Ticket ticket = ticket(10L, user(1L, Role.ROLE_USER), null, TicketStatus.OPEN);
		TicketResponse response = response(10L, "OPEN");

		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(ticketMapper.toResponse(ticket)).thenReturn(response);

		assertEquals(response, ticketService.getTicketById(10L));
	}

	@Test
	void getTicketById_whenMissingThrowsTicketNotFoundException() {
		when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(TicketNotFoundException.class, () -> ticketService.getTicketById(99L));
	}

	@Test
	void assignTicket_whenTicketOpenAssignsAgentAndMovesToInProgress() {
		User customer = user(1L, Role.ROLE_USER);
		User agent = user(2L, Role.ROLE_SUPPORT_AGENT);
		Ticket ticket = ticket(10L, customer, null, TicketStatus.OPEN);
		TicketResponse response = response(10L, "IN_PROGRESS");

		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(entityManager.find(User.class, 2L)).thenReturn(agent);
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response);

		assertEquals(response, ticketService.assignTicket(10L, new TicketAssignRequest(2L)));

		ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertEquals(agent, ticketCaptor.getValue().getAssignedAgent());
		assertEquals(TicketStatus.IN_PROGRESS, ticketCaptor.getValue().getStatus());
		verify(ticketSearchIndexService).index(ticket);
	}

	@Test
	void assignTicket_whenAgentIsAdminAssignsTicketAndMovesToInProgress() {
		User customer = user(1L, Role.ROLE_USER);
		User admin = user(3L, Role.ROLE_ADMIN);
		Ticket ticket = ticket(10L, customer, null, TicketStatus.OPEN);
		TicketResponse response = response(10L, "IN_PROGRESS");

		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(entityManager.find(User.class, 3L)).thenReturn(admin);
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response);

		assertEquals(response, ticketService.assignTicket(10L, new TicketAssignRequest(3L)));
		assertEquals(admin, ticket.getAssignedAgent());
		assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
		verify(ticketSearchIndexService).index(ticket);
	}

	@Test
	void assignTicket_whenTicketNotOpenThrowsBusinessRuleException() {
		Ticket ticket = ticket(10L, user(1L, Role.ROLE_USER), null, TicketStatus.IN_PROGRESS);
		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

		BusinessRuleException exception = assertThrows(
				BusinessRuleException.class,
				() -> ticketService.assignTicket(10L, new TicketAssignRequest(2L))
		);
		assertEquals("Only open tickets are assignable", exception.getMessage());
	}

	@Test
	void assignTicket_whenAgentIsRegularUserThrowsBusinessRuleException() {
		User agent = user(2L, Role.ROLE_USER);
		Ticket ticket = ticket(10L, user(1L, Role.ROLE_USER), null, TicketStatus.OPEN);

		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(entityManager.find(User.class, 2L)).thenReturn(agent);

		assertThrows(BusinessRuleException.class, () -> ticketService.assignTicket(10L, new TicketAssignRequest(2L)));
	}

	@Test
	void assignTicket_whenAgentDoesNotExistThrowsUserNotFoundException() {
		Ticket ticket = ticket(10L, user(1L, Role.ROLE_USER), null, TicketStatus.OPEN);

		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(entityManager.find(User.class, 2L)).thenReturn(null);

		assertThrows(UserNotFoundException.class, () -> ticketService.assignTicket(10L, new TicketAssignRequest(2L)));
	}

	@Test
	void resolveTicket_whenNotClosedMovesToResolvedAndPublishesEvent() {
		User customer = user(1L, Role.ROLE_USER);
		User agent = user(2L, Role.ROLE_SUPPORT_AGENT);
		Ticket ticket = ticket(10L, customer, agent, TicketStatus.IN_PROGRESS);
		TicketResponse response = response(10L, "RESOLVED");

		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response);

		assertEquals(response, ticketService.resolveTicket(10L));
		assertEquals(TicketStatus.RESOLVED, ticket.getStatus());
		verify(ticketSearchIndexService).index(ticket);
		verify(ticketEventProducer).publishTicketResolved(eq(ticket), anyLong());
	}

	@Test
	void resolveTicket_whenAlreadyResolvedReturnsResponseWithoutSavingOrPublishingAgain() {
		Ticket ticket = ticket(10L, user(1L, Role.ROLE_USER), user(2L, Role.ROLE_SUPPORT_AGENT), TicketStatus.RESOLVED);
		TicketResponse response = response(10L, "RESOLVED");

		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(ticketMapper.toResponse(ticket)).thenReturn(response);

		assertEquals(response, ticketService.resolveTicket(10L));
		verify(ticketRepository, never()).save(any(Ticket.class));
		verify(ticketSearchIndexService, never()).index(any(Ticket.class));
		verify(ticketEventProducer, never()).publishTicketResolved(any(Ticket.class), anyLong());
	}

	@Test
	void closeTicket_whenNotClosedMovesToClosed() {
		Ticket ticket = ticket(10L, user(1L, Role.ROLE_USER), null, TicketStatus.RESOLVED);
		TicketResponse response = response(10L, "CLOSED");

		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response);

		assertEquals(response, ticketService.closeTicket(10L));
		assertEquals(TicketStatus.CLOSED, ticket.getStatus());
		verify(ticketSearchIndexService).index(ticket);
	}

	@Test
	void modifyingClosedTicketThrowsBusinessRuleException() {
		Ticket ticket = ticket(10L, user(1L, Role.ROLE_USER), null, TicketStatus.CLOSED);
		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

		assertThrows(BusinessRuleException.class, () -> ticketService.assignTicket(10L, new TicketAssignRequest(2L)));
		assertThrows(BusinessRuleException.class, () -> ticketService.resolveTicket(10L));
		assertThrows(BusinessRuleException.class, () -> ticketService.closeTicket(10L));
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
				.createdAt(LocalDateTime.now().minusHours(2))
				.build();
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
