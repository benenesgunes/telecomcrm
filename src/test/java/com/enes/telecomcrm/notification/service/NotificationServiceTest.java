package com.enes.telecomcrm.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.enes.telecomcrm.subscription.event.SubscriptionActivatedEvent;
import com.enes.telecomcrm.subscription.event.SubscriptionActivatedPayload;
import com.enes.telecomcrm.ticket.event.TicketCreatedEvent;
import com.enes.telecomcrm.ticket.event.TicketCreatedPayload;
import com.enes.telecomcrm.ticket.event.TicketResolvedEvent;
import com.enes.telecomcrm.ticket.event.TicketResolvedPayload;

@ExtendWith(OutputCaptureExtension.class)
class NotificationServiceTest {

	private final NotificationService notificationService = new NotificationService();

	@Test
	void notificationHandlersGenerateLogs(CapturedOutput output) {
		notificationService.handleTicketCreated(TicketCreatedEvent.of(new TicketCreatedPayload(
				101L,
				"Internet drops every morning",
				"HIGH",
				5L,
				"john.doe@example.com"
		)));
		notificationService.handleTicketResolved(TicketResolvedEvent.of(new TicketResolvedPayload(
				101L,
				12L,
				1410L,
				5L,
				"john.doe@example.com"
		)));
		notificationService.handleSubscriptionActivated(SubscriptionActivatedEvent.of(new SubscriptionActivatedPayload(
				22L,
				5L,
				"john.doe@example.com",
				3L,
				"Home Internet 100Mbps",
				LocalDate.of(2024, 2, 1)
		)));

		assertThat(output).contains("Notification received TicketCreatedEvent");
		assertThat(output).contains("Notification received TicketResolvedEvent");
		assertThat(output).contains("Notification received SubscriptionActivatedEvent");
	}
}
