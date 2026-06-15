package com.enes.telecomcrm.subscription.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.subscription.dto.PlanRequest;
import com.enes.telecomcrm.subscription.dto.PlanResponse;
import com.enes.telecomcrm.subscription.entity.Plan;
import com.enes.telecomcrm.subscription.entity.PlanType;
import com.enes.telecomcrm.subscription.exception.PlanNotFoundException;
import com.enes.telecomcrm.subscription.mapper.PlanMapper;
import com.enes.telecomcrm.subscription.repository.PlanRepository;

class PlanServiceTest {

	private final PlanRepository planRepository = mock(PlanRepository.class);
	private final PlanMapper planMapper = mock(PlanMapper.class);
	private final PlanService planService = new PlanService(planRepository, planMapper);

	@Test
	void createPlan_savesPlanAndReturnsMappedResponse() {
		PlanRequest request = request("Mobile Starter");
		Plan mappedPlan = plan(null, "Mobile Starter");
		Plan savedPlan = plan(1L, "Mobile Starter");
		PlanResponse response = response(1L, "Mobile Starter");

		when(planRepository.existsByName("Mobile Starter")).thenReturn(false);
		when(planMapper.toEntity(request)).thenReturn(mappedPlan);
		when(planRepository.save(mappedPlan)).thenReturn(savedPlan);
		when(planMapper.toResponse(savedPlan)).thenReturn(response);

		assertEquals(response, planService.createPlan(request));
	}

	@Test
	void createPlan_whenNameExistsThrowsBusinessRuleException() {
		PlanRequest request = request("Mobile Starter");
		when(planRepository.existsByName("Mobile Starter")).thenReturn(true);

		assertThrows(BusinessRuleException.class, () -> planService.createPlan(request));
	}

	@Test
	void getAllPlans_returnsMappedResponses() {
		List<Plan> plans = List.of(plan(1L, "Mobile Starter"), plan(2L, "TV Plus"));
		List<PlanResponse> responses = List.of(response(1L, "Mobile Starter"), response(2L, "TV Plus"));

		when(planRepository.findAll()).thenReturn(plans);
		when(planMapper.toResponseList(plans)).thenReturn(responses);

		assertEquals(responses, planService.getAllPlans());
	}

	@Test
	void getPlanById_whenPlanExistsReturnsMappedResponse() {
		Plan plan = plan(1L, "Mobile Starter");
		PlanResponse response = response(1L, "Mobile Starter");

		when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
		when(planMapper.toResponse(plan)).thenReturn(response);

		assertEquals(response, planService.getPlanById(1L));
	}

	@Test
	void getPlanById_whenPlanDoesNotExistThrowsPlanNotFoundException() {
		when(planRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(PlanNotFoundException.class, () -> planService.getPlanById(99L));
	}

	@Test
	void updatePlan_updatesFieldsAndReturnsMappedResponse() {
		Plan existingPlan = plan(1L, "Old Plan");
		PlanRequest request = new PlanRequest(
				"Home Internet 100Mbps",
				PlanType.INTERNET,
				new BigDecimal("399.99"),
				"100Mbps home internet package"
		);
		PlanResponse response = response(1L, "Home Internet 100Mbps");

		when(planRepository.findById(1L)).thenReturn(Optional.of(existingPlan));
		when(planRepository.existsByNameAndIdNot("Home Internet 100Mbps", 1L)).thenReturn(false);
		when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(planMapper.toResponse(any(Plan.class))).thenReturn(response);

		assertEquals(response, planService.updatePlan(1L, request));

		ArgumentCaptor<Plan> planCaptor = ArgumentCaptor.forClass(Plan.class);
		verify(planRepository).save(planCaptor.capture());
		Plan savedPlan = planCaptor.getValue();

		assertEquals("Home Internet 100Mbps", savedPlan.getName());
		assertEquals(PlanType.INTERNET, savedPlan.getType());
		assertEquals(new BigDecimal("399.99"), savedPlan.getMonthlyPrice());
		assertEquals("100Mbps home internet package", savedPlan.getDescription());
	}

	@Test
	void updatePlan_whenNameBelongsToAnotherPlanThrowsBusinessRuleException() {
		Plan existingPlan = plan(1L, "Old Plan");
		PlanRequest request = request("Mobile Starter");

		when(planRepository.findById(1L)).thenReturn(Optional.of(existingPlan));
		when(planRepository.existsByNameAndIdNot("Mobile Starter", 1L)).thenReturn(true);

		assertThrows(BusinessRuleException.class, () -> planService.updatePlan(1L, request));
	}

	@Test
	void deletePlan_deletesExistingPlanAndReturnsMappedResponse() {
		Plan plan = plan(1L, "Mobile Starter");
		PlanResponse response = response(1L, "Mobile Starter");

		when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
		when(planMapper.toResponse(plan)).thenReturn(response);

		assertEquals(response, planService.deletePlan(1L));
		verify(planRepository).delete(plan);
	}

	private PlanRequest request(String name) {
		return new PlanRequest(name, PlanType.MOBILE, new BigDecimal("199.99"), "Basic mobile package");
	}

	private Plan plan(Long id, String name) {
		return Plan.builder()
				.id(id)
				.name(name)
				.type(PlanType.MOBILE)
				.monthlyPrice(new BigDecimal("199.99"))
				.description("Basic mobile package")
				.build();
	}

	private PlanResponse response(Long id, String name) {
		return new PlanResponse(id, name, "MOBILE", new BigDecimal("199.99"), "Basic mobile package");
	}
}
