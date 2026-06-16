package com.enes.telecomcrm.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import com.enes.telecomcrm.search.document.TicketDocument;
import com.enes.telecomcrm.search.document.UserDocument;
import com.enes.telecomcrm.ticket.dto.TicketResponse;
import com.enes.telecomcrm.user.dto.UserResponse;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;

class SearchServiceTest {

	private final ElasticsearchOperations elasticsearchOperations = mock(ElasticsearchOperations.class);
	private final SearchService searchService = new SearchService(elasticsearchOperations);

	@Test
	@SuppressWarnings("unchecked")
	void searchTickets_usesFuzzyAndPartialQueryAndMapsDocuments() {
		LocalDateTime now = LocalDateTime.now();
		TicketDocument document = TicketDocument.builder()
				.id("10")
				.title("Internet drops")
				.description("Connection drops every morning.")
				.status("OPEN")
				.priority("HIGH")
				.customerId(1L)
				.customerEmail("john@example.com")
				.assignedAgentId(2L)
				.createdAt(now)
				.updatedAt(now)
				.build();
		SearchHit<TicketDocument> hit = mock(SearchHit.class);
		SearchHits<TicketDocument> hits = mock(SearchHits.class);
		when(hit.getContent()).thenReturn(document);
		when(hits.getSearchHits()).thenReturn(List.of(hit));
		when(elasticsearchOperations.search(any(NativeQuery.class), eq(TicketDocument.class))).thenReturn(hits);

		List<TicketResponse> responses = searchService.searchTickets("intrnet");

		ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
		verify(elasticsearchOperations).search(queryCaptor.capture(), eq(TicketDocument.class));
		assertFuzzyAndPartialQuery(queryCaptor.getValue(), List.of("title", "description"), "intrnet");
		assertThat(responses).hasSize(1);
		assertThat(responses.getFirst().id()).isEqualTo(10L);
		assertThat(responses.getFirst().customer().email()).isEqualTo("john@example.com");
		assertThat(responses.getFirst().assignedAgent().id()).isEqualTo(2L);
	}

	@Test
	@SuppressWarnings("unchecked")
	void searchUsers_usesFuzzyAndPartialQueryAndMapsDocuments() {
		UserDocument document = UserDocument.builder()
				.id("1")
				.firstName("John")
				.lastName("Doe")
				.email("john@example.com")
				.role("ROLE_USER")
				.build();
		SearchHit<UserDocument> hit = mock(SearchHit.class);
		SearchHits<UserDocument> hits = mock(SearchHits.class);
		when(hit.getContent()).thenReturn(document);
		when(hits.getSearchHits()).thenReturn(List.of(hit));
		when(elasticsearchOperations.search(any(NativeQuery.class), eq(UserDocument.class))).thenReturn(hits);

		List<UserResponse> responses = searchService.searchUsers("jo");

		ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
		verify(elasticsearchOperations).search(queryCaptor.capture(), eq(UserDocument.class));
		assertFuzzyAndPartialQuery(queryCaptor.getValue(), List.of("firstName", "lastName", "email"), "jo");
		assertThat(responses).hasSize(1);
		assertThat(responses.getFirst().id()).isEqualTo(1L);
		assertThat(responses.getFirst().email()).isEqualTo("john@example.com");
		assertThat(responses.getFirst().role()).isEqualTo("ROLE_USER");
	}

	private void assertFuzzyAndPartialQuery(NativeQuery query, List<String> fields, String value) {
		var shouldClauses = query.getQuery().bool().should();

		assertThat(shouldClauses).hasSize(2);
		assertThat(shouldClauses.getFirst().multiMatch().query()).isEqualTo(value);
		assertThat(shouldClauses.getFirst().multiMatch().fields()).containsExactlyElementsOf(fields);
		assertThat(shouldClauses.getFirst().multiMatch().type()).isEqualTo(TextQueryType.BestFields);
		assertThat(shouldClauses.getFirst().multiMatch().fuzziness()).isEqualTo("AUTO");
		assertThat(shouldClauses.get(1).multiMatch().query()).isEqualTo(value);
		assertThat(shouldClauses.get(1).multiMatch().fields()).containsExactlyElementsOf(fields);
		assertThat(shouldClauses.get(1).multiMatch().type()).isEqualTo(TextQueryType.BoolPrefix);
		assertThat(query.getQuery().bool().minimumShouldMatch()).isEqualTo("1");
	}
}
