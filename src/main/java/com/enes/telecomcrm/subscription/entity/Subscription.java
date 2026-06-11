package com.enes.telecomcrm.subscription.entity;

import java.time.LocalDate;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
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
		name = "subscriptions",
		indexes = {
				@Index(name = "idx_subscriptions_user_id", columnList = "user_id"),
				@Index(name = "idx_subscriptions_plan_id", columnList = "plan_id"),
				@Index(name = "idx_subscriptions_status", columnList = "status")
		}
)
@Check(name = "chk_subscription_end_date", constraints = "end_date IS NULL OR end_date >= start_date")
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false, columnDefinition = "subscription_status")
	private SubscriptionStatus status;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "user_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_subscriptions_user")
	)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "plan_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_subscriptions_plan")
	)
	private Plan plan;
}
