package com.enes.telecomcrm.auth.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.enes.telecomcrm.subscription.repository.SubscriptionRepository;
import com.enes.telecomcrm.ticket.repository.TicketRepository;
import com.enes.telecomcrm.user.entity.Role;

class AuthorizationServiceTest {

	private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
	private final TicketRepository ticketRepository = mock(TicketRepository.class);
	private final AuthorizationService authorizationService = new AuthorizationService(subscriptionRepository, ticketRepository);

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void canAccessUser_allowsAdminAndSelfButRejectsOtherUser() {
		authenticate(1L, Role.ROLE_ADMIN);
		assertTrue(authorizationService.canAccessUser(99L));

		authenticate(1L, Role.ROLE_USER);
		assertTrue(authorizationService.canAccessUser(1L));
		assertFalse(authorizationService.canAccessUser(2L));
	}

	@Test
	void canAccessSubscription_allowsOwnerAndAdminOnly() {
		authenticate(1L, Role.ROLE_USER);
		when(subscriptionRepository.existsByIdAndUserId(10L, 1L)).thenReturn(true);
		when(subscriptionRepository.existsByIdAndUserId(20L, 1L)).thenReturn(false);

		assertTrue(authorizationService.canAccessSubscription(10L));
		assertFalse(authorizationService.canAccessSubscription(20L));

		authenticate(2L, Role.ROLE_ADMIN);
		assertTrue(authorizationService.canAccessSubscription(20L));
	}

	@Test
	void canAccessTicket_allowsOwnerSupportAgentAndAdmin() {
		authenticate(1L, Role.ROLE_USER);
		when(ticketRepository.existsByIdAndCustomerId(10L, 1L)).thenReturn(true);
		when(ticketRepository.existsByIdAndCustomerId(20L, 1L)).thenReturn(false);

		assertTrue(authorizationService.canAccessTicket(10L));
		assertFalse(authorizationService.canAccessTicket(20L));

		authenticate(2L, Role.ROLE_SUPPORT_AGENT);
		assertTrue(authorizationService.canAccessTicket(20L));

		authenticate(3L, Role.ROLE_ADMIN);
		assertTrue(authorizationService.canAccessTicket(20L));
	}

	@Test
	void canAccessTicket_asSupportAgentDoesNotNeedOwnershipLookup() {
		authenticate(2L, Role.ROLE_SUPPORT_AGENT);

		assertTrue(authorizationService.canAccessTicket(20L));
		verifyNoInteractions(ticketRepository);
	}

	@Test
	void canAccessTicketComments_allowsOwnerAssignedAgentAndAdminOnly() {
		authenticate(1L, Role.ROLE_USER);
		when(ticketRepository.existsByIdAndCustomerId(10L, 1L)).thenReturn(true);
		when(ticketRepository.existsByIdAndCustomerId(20L, 1L)).thenReturn(false);

		assertTrue(authorizationService.canAccessTicketComments(10L));
		assertFalse(authorizationService.canAccessTicketComments(20L));

		authenticate(2L, Role.ROLE_SUPPORT_AGENT);
		when(ticketRepository.existsByIdAndCustomerId(30L, 2L)).thenReturn(false);
		when(ticketRepository.existsByIdAndAssignedAgentId(30L, 2L)).thenReturn(true);
		when(ticketRepository.existsByIdAndCustomerId(40L, 2L)).thenReturn(false);
		when(ticketRepository.existsByIdAndAssignedAgentId(40L, 2L)).thenReturn(false);

		assertTrue(authorizationService.canAccessTicketComments(30L));
		assertFalse(authorizationService.canAccessTicketComments(40L));

		authenticate(3L, Role.ROLE_ADMIN);
		assertTrue(authorizationService.canAccessTicketComments(99L));
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
}
