package com.enes.telecomcrm.subscription.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.enes.telecomcrm.common.exception.GlobalExceptionHandler;
import com.enes.telecomcrm.subscription.dto.PlanRequest;
import com.enes.telecomcrm.subscription.dto.PlanResponse;
import com.enes.telecomcrm.subscription.entity.PlanType;
import com.enes.telecomcrm.subscription.exception.PlanNotFoundException;
import com.enes.telecomcrm.subscription.service.PlanService;
import com.fasterxml.jackson.databind.ObjectMapper;

class PlanControllerTest {

	private final PlanService planService = mock(PlanService.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new PlanController(planService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void createPlan_returnsPlanResponseDto() throws Exception {
		PlanRequest request = request("Mobile Starter");
		PlanResponse response = response(1L, "Mobile Starter");

		when(planService.createPlan(request)).thenReturn(response);

		mockMvc.perform(post("/api/v1/plans")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Plan created successfully"))
				.andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.name").value("Mobile Starter"))
				.andExpect(jsonPath("$.data.type").value("MOBILE"));
	}

	@Test
	void getAllPlans_returnsPlanResponseDtos() throws Exception {
		when(planService.getAllPlans()).thenReturn(List.of(response(1L, "Mobile Starter"), response(2L, "TV Plus")));

		mockMvc.perform(get("/api/v1/plans"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Plans retrieved successfully"))
				.andExpect(jsonPath("$.data[0].id").value(1))
				.andExpect(jsonPath("$.data[1].name").value("TV Plus"));
	}

	@Test
	void getPlanById_returnsPlanResponseDto() throws Exception {
		when(planService.getPlanById(1L)).thenReturn(response(1L, "Mobile Starter"));

		mockMvc.perform(get("/api/v1/plans/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Plan retrieved successfully"))
				.andExpect(jsonPath("$.data.id").value(1));
	}

	@Test
	void updatePlan_returnsUpdatedPlanResponseDto() throws Exception {
		PlanRequest request = request("Mobile Starter Plus");
		PlanResponse response = response(1L, "Mobile Starter Plus");

		when(planService.updatePlan(1L, request)).thenReturn(response);

		mockMvc.perform(put("/api/v1/plans/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Plan updated successfully"))
				.andExpect(jsonPath("$.data.name").value("Mobile Starter Plus"));
	}

	@Test
	void updatePlan_whenRequestIsInvalidReturnsBadRequest() throws Exception {
		String invalidRequest = """
				{
				  "name": "",
				  "type": null,
				  "monthlyPrice": 0,
				  "description": "Invalid"
				}
				""";

		mockMvc.perform(put("/api/v1/plans/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidRequest))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void deletePlan_returnsDeletedPlanResponseDto() throws Exception {
		when(planService.deletePlan(1L)).thenReturn(response(1L, "Mobile Starter"));

		mockMvc.perform(delete("/api/v1/plans/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Plan deleted successfully"))
				.andExpect(jsonPath("$.data.id").value(1));
	}

	@Test
	void getPlanById_whenPlanDoesNotExistReturnsNotFound() throws Exception {
		when(planService.getPlanById(99L)).thenThrow(new PlanNotFoundException(99L));

		mockMvc.perform(get("/api/v1/plans/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Plan not found with id: 99"));
	}

	private PlanRequest request(String name) {
		return new PlanRequest(name, PlanType.MOBILE, new BigDecimal("199.99"), "Basic mobile package");
	}

	private PlanResponse response(Long id, String name) {
		return new PlanResponse(id, name, "MOBILE", new BigDecimal("199.99"), "Basic mobile package");
	}
}
