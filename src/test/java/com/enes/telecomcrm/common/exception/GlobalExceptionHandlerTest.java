package com.enes.telecomcrm.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TestExceptionController())
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void resourceNotFoundExceptionReturnsNotFoundApiResponse() throws Exception {
		mockMvc.perform(get("/test-exceptions/not-found"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Resource not found"))
				.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void businessRuleExceptionReturnsUnprocessableEntityApiResponse() throws Exception {
		mockMvc.perform(get("/test-exceptions/business-rule"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Business rule violated"))
				.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void unauthorizedExceptionReturnsForbiddenApiResponse() throws Exception {
		mockMvc.perform(get("/test-exceptions/unauthorized"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Insufficient permissions"))
				.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void validationExceptionReturnsBadRequestApiResponse() throws Exception {
		mockMvc.perform(post("/test-exceptions/validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("name: must not be blank"))
				.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void unexpectedExceptionReturnsInternalServerErrorApiResponse() throws Exception {
		mockMvc.perform(get("/test-exceptions/unexpected"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Unexpected server error"))
				.andExpect(jsonPath("$.data").doesNotExist());
	}

	@RestController
	@RequestMapping("/test-exceptions")
	private static class TestExceptionController {

		@GetMapping("/not-found")
		void notFound() {
			throw new ResourceNotFoundException("Resource not found");
		}

		@GetMapping("/business-rule")
		void businessRule() {
			throw new BusinessRuleException("Business rule violated");
		}

		@GetMapping("/unauthorized")
		void unauthorized() {
			throw new UnauthorizedException("Insufficient permissions");
		}

		@PostMapping("/validation")
		void validation(@Valid @RequestBody TestRequest request) {
		}

		@GetMapping("/unexpected")
		void unexpected() {
			throw new IllegalStateException("Internal implementation detail");
		}
	}

	private record TestRequest(@NotBlank String name) {
	}
}
