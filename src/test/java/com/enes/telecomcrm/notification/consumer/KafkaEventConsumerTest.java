package com.enes.telecomcrm.notification.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.enes.telecomcrm.analytics.service.DashboardService;
import com.enes.telecomcrm.notification.service.NotificationService;
import com.enes.telecomcrm.subscription.event.SubscriptionActivatedEvent;
import com.enes.telecomcrm.subscription.event.SubscriptionActivatedPayload;
import com.enes.telecomcrm.ticket.event.TicketCreatedEvent;
import com.enes.telecomcrm.ticket.event.TicketCreatedPayload;
import com.enes.telecomcrm.ticket.event.TicketResolvedEvent;
import com.enes.telecomcrm.ticket.event.TicketResolvedPayload;

@ExtendWith(OutputCaptureExtension.class)
class KafkaEventConsumerTest {

	private final NotificationService notificationService = mock(NotificationService.class);
	private final DashboardService dashboardService = mock(DashboardService.class);
	private final KafkaEventConsumer kafkaEventConsumer = new KafkaEventConsumer(notificationService, dashboardService);

	@Test
	void consumeTicketCreated_logsEventAndDelegatesToNotificationService(CapturedOutput output) {
		TicketCreatedEvent event = TicketCreatedEvent.of(new TicketCreatedPayload(
				101L,
				"Internet drops every morning",
				"HIGH",
				5L,
				"john.doe@example.com"
		));

		kafkaEventConsumer.consumeTicketCreated(event);

		verify(notificationService).handleTicketCreated(event);
		assertThat(output).contains("Consumed TicketCreatedEvent");
		assertThat(output).contains("ticketId=101");
	}

	@Test
	void consumeTicketResolved_logsEventAndDelegatesToNotificationAndAnalytics(CapturedOutput output) {
		TicketResolvedEvent event = TicketResolvedEvent.of(new TicketResolvedPayload(
				101L,
				12L,
				1410L,
				5L,
				"john.doe@example.com"
		));

		kafkaEventConsumer.consumeTicketResolved(event);

		verify(notificationService).handleTicketResolved(event);
		verify(dashboardService).processTicketResolvedEvent(event);
		assertThat(output).contains("Consumed TicketResolvedEvent");
		assertThat(output).contains("ticketId=101");
	}

	@Test
	void consumeSubscriptionActivated_logsEventAndDelegatesToNotificationService(CapturedOutput output) {
		SubscriptionActivatedEvent event = SubscriptionActivatedEvent.of(new SubscriptionActivatedPayload(
				22L,
				5L,
				"john.doe@example.com",
				3L,
				"Home Internet 100Mbps",
				LocalDate.of(2024, 2, 1)
		));

		kafkaEventConsumer.consumeSubscriptionActivated(event);

		verify(notificationService).handleSubscriptionActivated(event);
		assertThat(output).contains("Consumed SubscriptionActivatedEvent");
		assertThat(output).contains("subscriptionId=22");
	}
}
