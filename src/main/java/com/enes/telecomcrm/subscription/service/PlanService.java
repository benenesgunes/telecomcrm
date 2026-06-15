package com.enes.telecomcrm.subscription.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.subscription.dto.PlanRequest;
import com.enes.telecomcrm.subscription.dto.PlanResponse;
import com.enes.telecomcrm.subscription.entity.Plan;
import com.enes.telecomcrm.subscription.exception.PlanNotFoundException;
import com.enes.telecomcrm.subscription.mapper.PlanMapper;
import com.enes.telecomcrm.subscription.repository.PlanRepository;

@Service
public class PlanService {

	private final PlanRepository planRepository;
	private final PlanMapper planMapper;

	public PlanService(PlanRepository planRepository, PlanMapper planMapper) {
		this.planRepository = planRepository;
		this.planMapper = planMapper;
	}

	@Transactional
	public PlanResponse createPlan(PlanRequest request) {
		if (planRepository.existsByName(request.name())) {
			throw new BusinessRuleException("Plan name is already in use");
		}

		Plan plan = planMapper.toEntity(request);
		return planMapper.toResponse(planRepository.save(plan));
	}

	@Transactional(readOnly = true)
	public List<PlanResponse> getAllPlans() {
		return planMapper.toResponseList(planRepository.findAll());
	}

	@Transactional(readOnly = true)
	public PlanResponse getPlanById(Long id) {
		return planMapper.toResponse(findPlanById(id));
	}

	@Transactional
	public PlanResponse updatePlan(Long id, PlanRequest request) {
		Plan plan = findPlanById(id);

		if (planRepository.existsByNameAndIdNot(request.name(), id)) {
			throw new BusinessRuleException("Plan name is already in use");
		}

		plan.setName(request.name());
		plan.setType(request.type());
		plan.setMonthlyPrice(request.monthlyPrice());
		plan.setDescription(request.description());

		return planMapper.toResponse(planRepository.save(plan));
	}

	@Transactional
	public PlanResponse deletePlan(Long id) {
		Plan plan = findPlanById(id);
		PlanResponse response = planMapper.toResponse(plan);
		planRepository.delete(plan);
		return response;
	}

	private Plan findPlanById(Long id) {
		return planRepository.findById(id)
				.orElseThrow(() -> new PlanNotFoundException(id));
	}
}
