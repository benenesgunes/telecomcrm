package com.enes.telecomcrm.subscription.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name = "plans",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_plans_name", columnNames = "name")
		},
		indexes = {
				@Index(name = "idx_plans_type", columnList = "type")
		}
)
@Check(name = "chk_plans_monthly_price", constraints = "monthly_price > 0")
public class Plan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, unique = true, length = 150)
	private String name;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "type", nullable = false, columnDefinition = "plan_type")
	private PlanType type;

	@Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal monthlyPrice;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Builder.Default
	@OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
	private List<Subscription> subscriptions = new ArrayList<>();
}
