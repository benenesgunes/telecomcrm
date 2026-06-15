package com.enes.telecomcrm.subscription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.enes.telecomcrm.subscription.entity.Plan;
import com.enes.telecomcrm.subscription.entity.PlanType;

public interface PlanRepository extends JpaRepository<Plan, Long> {

	Optional<Plan> findByName(String name);

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);

	List<Plan> findByType(PlanType type);

	@Query("""
			select p.id as planId,
			       p.name as planName,
			       count(s.id) as activeSubscriberCount
			from Plan p
			left join p.subscriptions s
				with s.status = com.enes.telecomcrm.subscription.entity.SubscriptionStatus.ACTIVE
			group by p.id, p.name
			order by count(s.id) desc, p.name asc
			""")
	List<PlanPopularityView> findPopularPlans();

	interface PlanPopularityView {

		Long getPlanId();

		String getPlanName();

		long getActiveSubscriberCount();
	}
}
