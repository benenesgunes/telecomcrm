package com.enes.telecomcrm.search.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "tickets", createIndex = false)
public class TicketDocument {

	@Id
	private String id;

	@Field(type = FieldType.Text, analyzer = "standard")
	private String title;

	@Field(type = FieldType.Text, analyzer = "standard")
	private String description;

	@Field(type = FieldType.Keyword)
	private String status;

	@Field(type = FieldType.Keyword)
	private String priority;

	@Field(type = FieldType.Long)
	private Long customerId;

	@Field(type = FieldType.Keyword)
	private String customerEmail;

	@Field(type = FieldType.Long)
	private Long assignedAgentId;

	@Field(type = FieldType.Date, format = DateFormat.strict_date_optional_time)
	private LocalDateTime createdAt;

	@Field(type = FieldType.Date, format = DateFormat.strict_date_optional_time)
	private LocalDateTime updatedAt;
}
