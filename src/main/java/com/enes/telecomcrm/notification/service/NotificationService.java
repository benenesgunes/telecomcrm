package com.enes.telecomcrm.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.enes.telecomcrm.subscription.event.SubscriptionActivatedEvent;
import com.enes.telecomcrm.ticket.event.TicketCreatedEvent;
import com.enes.telecomcrm.ticket.event.TicketResolvedEvent;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	public void handleTicketCreated(TicketCreatedEvent event) {
		log.info(
				"Notification received TicketCreatedEvent for ticketId={}, customerEmail={}",
				event.payload().ticketId(),
				event.payload().customerEmail()
		);
	}

	public void handleTicketResolved(TicketResolvedEvent event) {
		log.info(
				"Notification received TicketResolvedEvent for ticketId={}, customerEmail={}",
				event.payload().ticketId(),
				event.payload().customerEmail()
		);
	}

	public void handleSubscriptionActivated(SubscriptionActivatedEvent event) {
		log.info(
				"Notification received SubscriptionActivatedEvent for subscriptionId={}, userEmail={}",
				event.payload().subscriptionId(),
				event.payload().userEmail()
		);
	}
}
