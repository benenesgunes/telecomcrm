package com.enes.telecomcrm.auth.security;

import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.enes.telecomcrm.subscription.repository.SubscriptionRepository;
import com.enes.telecomcrm.ticket.repository.TicketRepository;

@Service
public class AuthorizationService {

	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String ROLE_SUPPORT_AGENT = "ROLE_SUPPORT_AGENT";

	private final SubscriptionRepository subscriptionRepository;
	private final TicketRepository ticketRepository;

	public AuthorizationService(SubscriptionRepository subscriptionRepository, TicketRepository ticketRepository) {
		this.subscriptionRepository = subscriptionRepository;
		this.ticketRepository = ticketRepository;
	}

	public boolean canAccessUser(Long userId) {
		Authentication authentication = currentAuthentication();
		return hasAuthority(authentication, ROLE_ADMIN) || isCurrentUser(authentication, userId);
	}

	public boolean canAccessUserSubscriptions(Long userId) {
		Authentication authentication = currentAuthentication();
		return hasAuthority(authentication, ROLE_ADMIN) || isCurrentUser(authentication, userId);
	}

	public boolean canAccessSubscription(Long subscriptionId) {
		Authentication authentication = currentAuthentication();
		if (hasAuthority(authentication, ROLE_ADMIN)) {
			return true;
		}
		Long currentUserId = currentUserId(authentication);
		return currentUserId != null
				&& subscriptionRepository.existsByIdAndUserId(subscriptionId, currentUserId);
	}

	public boolean canAccessTicket(Long ticketId) {
		Authentication authentication = currentAuthentication();
		if (hasAuthority(authentication, ROLE_ADMIN) || hasAuthority(authentication, ROLE_SUPPORT_AGENT)) {
			return true;
		}
		Long currentUserId = currentUserId(authentication);
		return currentUserId != null
				&& ticketRepository.existsByIdAndCustomerId(ticketId, currentUserId);
	}

	public boolean canAccessTicketComments(Long ticketId) {
		Authentication authentication = currentAuthentication();
		if (hasAuthority(authentication, ROLE_ADMIN)) {
			return true;
		}

		Long currentUserId = currentUserId(authentication);
		if (currentUserId == null) {
			return false;
		}

		if (ticketRepository.existsByIdAndCustomerId(ticketId, currentUserId)) {
			return true;
		}

		return hasAuthority(authentication, ROLE_SUPPORT_AGENT)
				&& ticketRepository.existsByIdAndAssignedAgentId(ticketId, currentUserId);
	}

	private Authentication currentAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	private boolean hasAuthority(Authentication authentication, String authority) {
		return authentication != null
				&& authentication.getAuthorities()
				.stream()
				.anyMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()));
	}

	private boolean isCurrentUser(Authentication authentication, Long userId) {
		Long currentUserId = currentUserId(authentication);
		return currentUserId != null && Objects.equals(currentUserId, userId);
	}

	private Long currentUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
			return null;
		}
		return userPrincipal.getId();
	}
}
