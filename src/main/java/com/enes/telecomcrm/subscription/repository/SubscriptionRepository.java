package com.enes.telecomcrm.subscription.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.enes.telecomcrm.subscription.entity.Subscription;
import com.enes.telecomcrm.subscription.entity.SubscriptionStatus;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

	List<Subscription> findByUserId(Long userId);

	List<Subscription> findByPlanId(Long planId);

	long countByStatus(SubscriptionStatus status);

	boolean existsByUserIdAndStatus(Long userId, SubscriptionStatus status);

	boolean existsByUserIdAndPlanIdAndStatus(Long userId, Long planId, SubscriptionStatus status);

	@Query("""
			select s.status as status,
			       count(s.id) as subscriptionCount
			from Subscription s
			group by s.status
			""")
	List<SubscriptionStatusCountView> countByStatusDistribution();

	@Query("""
			select year(s.startDate) as year,
			       month(s.startDate) as month,
			       count(s.id) as subscriptionCount
			from Subscription s
			where s.startDate >= :fromDate
			group by year(s.startDate), month(s.startDate)
			order by year(s.startDate), month(s.startDate)
			""")
	List<MonthlySubscriptionGrowthView> countMonthlyGrowthSince(@Param("fromDate") LocalDate fromDate);

	interface SubscriptionStatusCountView {

		SubscriptionStatus getStatus();

		long getSubscriptionCount();
	}

	interface MonthlySubscriptionGrowthView {

		Integer getYear();

		Integer getMonth();

		long getSubscriptionCount();
	}
}
