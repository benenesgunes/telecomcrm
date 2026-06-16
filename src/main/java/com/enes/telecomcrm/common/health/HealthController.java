package com.enes.telecomcrm.common.health;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Health", description = "Application and database health checks")
public class HealthController {

	private final DataSource dataSource;

	public HealthController(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@GetMapping("/health")
	@Operation(summary = "Health check", description = "Checks application availability and validates the database connection.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Application and database are up")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Database is unavailable")
	public ResponseEntity<Map<String, Object>> health() {
		try (Connection connection = dataSource.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("select 1");

			return ResponseEntity.ok(Map.of(
					"status", "UP",
					"database", "UP",
					"validConnection", connection.isValid(2)));
		}
		catch (SQLException ex) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(Map.of(
							"status", "DOWN",
							"database", "DOWN",
							"error", ex.getMessage()));
		}
	}
}
