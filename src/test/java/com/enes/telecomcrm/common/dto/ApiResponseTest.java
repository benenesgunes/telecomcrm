package com.enes.telecomcrm.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

	@Test
	void successCreatesSuccessfulEnvelope() {
		ApiResponse<String> response = ApiResponse.success("Operation completed", "payload");

		assertThat(response.isSuccess()).isTrue();
		assertThat(response.getMessage()).isEqualTo("Operation completed");
		assertThat(response.getData()).isEqualTo("payload");
	}

	@Test
	void errorCreatesErrorEnvelope() {
		ApiResponse<Object> response = ApiResponse.error("Descriptive error message");

		assertThat(response.isSuccess()).isFalse();
		assertThat(response.getMessage()).isEqualTo("Descriptive error message");
		assertThat(response.getData()).isNull();
	}
}
