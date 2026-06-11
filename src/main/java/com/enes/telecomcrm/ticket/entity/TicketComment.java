package com.enes.telecomcrm.ticket.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.enes.telecomcrm.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name = "ticket_comments",
		indexes = {
				@Index(name = "idx_ticket_comments_ticket_id", columnList = "ticket_id"),
				@Index(name = "idx_ticket_comments_author_id", columnList = "author_id")
		}
)
public class TicketComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "message", nullable = false, columnDefinition = "TEXT")
	private String message;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "author_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_ticket_comments_author")
	)
	private User author;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "ticket_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_ticket_comments_ticket")
	)
	private Ticket ticket;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
