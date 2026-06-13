package com.enes.telecomcrm.subscription.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.enes.telecomcrm.subscription.dto.PlanRequest;
import com.enes.telecomcrm.subscription.dto.PlanResponse;
import com.enes.telecomcrm.subscription.entity.Plan;

@Mapper(componentModel = "spring")
public interface PlanMapper {

	PlanResponse toResponse(Plan plan);

	List<PlanResponse> toResponseList(List<Plan> plans);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "subscriptions", ignore = true)
	Plan toEntity(PlanRequest request);
}
