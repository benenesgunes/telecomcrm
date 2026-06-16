package com.enes.telecomcrm.common.dto;

import lombok.Builder;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Builder
@Schema(description = "Standard API response envelope")
public class ApiResponse<T> {

	@Schema(description = "Whether the request succeeded", example = "true")
	private final boolean success;
	@Schema(description = "Human-readable response message", example = "Operation completed successfully")
	private final String message;
	@Schema(description = "Response payload")
	private final T data;

	public static <T> ApiResponse<T> success(String message, T data) {
		return ApiResponse.<T>builder()
				.success(true)
				.message(message)
				.data(data)
				.build();
	}

	public static <T> ApiResponse<T> error(String message) {
		return ApiResponse.<T>builder()
				.success(false)
				.message(message)
				.data(null)
				.build();
	}
}
