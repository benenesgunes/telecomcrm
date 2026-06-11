package com.enes.telecomcrm.ticket.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.enes.telecomcrm.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
		name = "tickets",
		indexes = {
				@Index(name = "idx_tickets_customer_id", columnList = "customer_id"),
				@Index(name = "idx_tickets_assigned_agent_id", columnList = "assigned_agent_id"),
				@Index(name = "idx_tickets_status", columnList = "status"),
				@Index(name = "idx_tickets_priority", columnList = "priority")
		}
)
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "description", nullable = false, columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false, columnDefinition = "ticket_status")
	private TicketStatus status;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "priority", nullable = false, columnDefinition = "ticket_priority")
	private TicketPriority priority;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "customer_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_tickets_customer")
	)
	private User customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "assigned_agent_id",
			foreignKey = @ForeignKey(name = "fk_tickets_assigned_agent")
	)
	private User assignedAgent;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder.Default
	@OneToMany(mappedBy = "ticket", fetch = FetchType.LAZY)
	private List<TicketComment> comments = new ArrayList<>();
}
