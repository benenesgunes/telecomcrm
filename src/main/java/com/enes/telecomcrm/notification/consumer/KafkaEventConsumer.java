package com.enes.telecomcrm.notification.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.enes.telecomcrm.analytics.service.DashboardService;
import com.enes.telecomcrm.notification.service.NotificationService;
import com.enes.telecomcrm.subscription.event.SubscriptionActivatedEvent;
import com.enes.telecomcrm.ticket.event.TicketCreatedEvent;
import com.enes.telecomcrm.ticket.event.TicketResolvedEvent;

@Component
public class KafkaEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

	private final NotificationService notificationService;
	private final DashboardService dashboardService;

	public KafkaEventConsumer(NotificationService notificationService, DashboardService dashboardService) {
		this.notificationService = notificationService;
		this.dashboardService = dashboardService;
	}

	@KafkaListener(
			topics = "${app.kafka.topics.ticket-created:ticket-created}",
			groupId = "${spring.kafka.consumer.group-id:telecom-crm-group}"
	)
	public void consumeTicketCreated(TicketCreatedEvent event) {
		log.info("Consumed TicketCreatedEvent eventId={}, ticketId={}", event.eventId(), event.payload().ticketId());
		notificationService.handleTicketCreated(event);
	}

	@KafkaListener(
			topics = "${app.kafka.topics.ticket-resolved:ticket-resolved}",
			groupId = "${spring.kafka.consumer.group-id:telecom-crm-group}"
	)
	public void consumeTicketResolved(TicketResolvedEvent event) {
		log.info("Consumed TicketResolvedEvent eventId={}, ticketId={}", event.eventId(), event.payload().ticketId());
		notificationService.handleTicketResolved(event);
		dashboardService.processTicketResolvedEvent(event);
	}

	@KafkaListener(
			topics = "${app.kafka.topics.subscription-activated:subscription-activated}",
			groupId = "${spring.kafka.consumer.group-id:telecom-crm-group}"
	)
	public void consumeSubscriptionActivated(SubscriptionActivatedEvent event) {
		log.info(
				"Consumed SubscriptionActivatedEvent eventId={}, subscriptionId={}",
				event.eventId(),
				event.payload().subscriptionId()
		);
		notificationService.handleSubscriptionActivated(event);
	}
}
