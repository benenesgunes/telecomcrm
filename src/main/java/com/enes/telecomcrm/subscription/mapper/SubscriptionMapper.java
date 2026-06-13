package com.enes.telecomcrm.subscription.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.enes.telecomcrm.subscription.dto.SubscriptionRequest;
import com.enes.telecomcrm.subscription.dto.SubscriptionResponse;
import com.enes.telecomcrm.subscription.entity.Plan;
import com.enes.telecomcrm.subscription.entity.Subscription;
import com.enes.telecomcrm.user.entity.User;

@Mapper(componentModel = "spring", uses = PlanMapper.class)
public interface SubscriptionMapper {

	SubscriptionResponse toResponse(Subscription subscription);

	List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "endDate", ignore = true)
	@Mapping(target = "user", source = "userId")
	@Mapping(target = "plan", source = "planId")
	Subscription toEntity(SubscriptionRequest request);

	default User userFromId(Long id) {
		if (id == null) {
			return null;
		}
		User user = new User();
		user.setId(id);
		return user;
	}

	default Plan planFromId(Long id) {
		if (id == null) {
			return null;
		}
		Plan plan = new Plan();
		plan.setId(id);
		return plan;
	}
}
