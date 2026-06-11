package com.enes.telecomcrm.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.enes.telecomcrm.subscription.entity.Subscription;
import com.enes.telecomcrm.ticket.entity.Ticket;
import com.enes.telecomcrm.ticket.entity.TicketComment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name = "users",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_users_email", columnNames = "email")
		},
		indexes = {
				@Index(name = "idx_users_email", columnList = "email"),
				@Index(name = "idx_users_role", columnList = "role")
		}
)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "role", nullable = false, columnDefinition = "user_role")
	private Role role;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder.Default
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<Subscription> subscriptions = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
	private List<Ticket> customerTickets = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "assignedAgent", fetch = FetchType.LAZY)
	private List<Ticket> assignedTickets = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
	private List<TicketComment> comments = new ArrayList<>();
}
