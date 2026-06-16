package com.enes.telecomcrm.search.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

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
@Document(indexName = "users", createIndex = false)
public class UserDocument {

	@Id
	private String id;

	@Field(type = FieldType.Text, analyzer = "standard")
	private String firstName;

	@Field(type = FieldType.Text, analyzer = "standard")
	private String lastName;

	@MultiField(
			mainField = @Field(type = FieldType.Text, analyzer = "standard"),
			otherFields = {@InnerField(suffix = "keyword", type = FieldType.Keyword)}
	)
	private String email;

	@Field(type = FieldType.Keyword)
	private String role;
}
