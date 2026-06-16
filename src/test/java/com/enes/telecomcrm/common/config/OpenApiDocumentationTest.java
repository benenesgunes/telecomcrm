package com.enes.telecomcrm.common.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void openApiDocsExposeAllEndpointsAndJwtSecurityScheme() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").exists())
				.andExpect(jsonPath("$.info.title").value("Telecom CRM API"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
				.andExpect(jsonPath("$.paths['/health'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/auth/register'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/auth/login'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/users'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/users/{id}'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/users/{id}'].put").exists())
				.andExpect(jsonPath("$.paths['/api/v1/users/{id}'].delete").exists())
				.andExpect(jsonPath("$.paths['/api/v1/plans'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/plans'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/plans/{id}'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/plans/{id}'].put").exists())
				.andExpect(jsonPath("$.paths['/api/v1/plans/{id}'].delete").exists())
				.andExpect(jsonPath("$.paths['/api/v1/subscriptions'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/subscriptions'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/subscriptions/{id}'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/subscriptions/user/{userId}'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/subscriptions/{id}/suspend'].patch").exists())
				.andExpect(jsonPath("$.paths['/api/v1/subscriptions/{id}/cancel'].patch").exists())
				.andExpect(jsonPath("$.paths['/api/v1/tickets'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/tickets'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/tickets/my'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/tickets/{id}'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/tickets/{id}/assign'].patch").exists())
				.andExpect(jsonPath("$.paths['/api/v1/tickets/{id}/resolve'].patch").exists())
				.andExpect(jsonPath("$.paths['/api/v1/tickets/{id}/close'].patch").exists())
				.andExpect(jsonPath("$.paths['/api/v1/tickets/{ticketId}/comments'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/tickets/{ticketId}/comments'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/search/tickets'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/search/users'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/dashboard/admin'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/dashboard/subscriptions'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/dashboard/tickets'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/dashboard/agent'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/users'].get.security[0].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/api/v1/search/tickets'].get.security[0].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security").doesNotExist())
				.andExpect(jsonPath("$.components.schemas.RegisterRequest").exists())
				.andExpect(jsonPath("$.components.schemas.TicketRequest").exists())
				.andExpect(jsonPath("$.components.schemas.AdminDashboardResponse").exists());
	}

	@Test
	void swaggerUiIsPubliclyReachable() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
	}
}
