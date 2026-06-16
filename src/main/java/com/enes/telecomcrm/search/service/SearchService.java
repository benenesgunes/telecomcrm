package com.enes.telecomcrm.search.service;

import java.util.List;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enes.telecomcrm.search.document.TicketDocument;
import com.enes.telecomcrm.search.document.UserDocument;
import com.enes.telecomcrm.ticket.dto.TicketResponse;
import com.enes.telecomcrm.user.dto.UserResponse;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;

@Service
public class SearchService {

	private final ElasticsearchOperations elasticsearchOperations;

	public SearchService(ElasticsearchOperations elasticsearchOperations) {
		this.elasticsearchOperations = elasticsearchOperations;
	}

	@Transactional(readOnly = true)
	public List<TicketResponse> searchTickets(String query) {
		NativeQuery searchQuery = searchQuery(query, List.of("title", "description"));
		return elasticsearchOperations.search(searchQuery, TicketDocument.class)
				.getSearchHits()
				.stream()
				.map(hit -> toTicketResponse(hit.getContent()))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<UserResponse> searchUsers(String query) {
		NativeQuery searchQuery = searchQuery(query, List.of("firstName", "lastName", "email"));
		return elasticsearchOperations.search(searchQuery, UserDocument.class)
				.getSearchHits()
				.stream()
				.map(hit -> toUserResponse(hit.getContent()))
				.toList();
	}

	private NativeQuery searchQuery(String query, List<String> fields) {
		return NativeQuery.builder()
				.withQuery(q -> q.bool(bool -> bool
						.should(should -> should.multiMatch(multiMatch -> multiMatch
								.query(query)
								.fields(fields)
								.type(TextQueryType.BestFields)
								.fuzziness("AUTO")
						))
						.should(should -> should.multiMatch(multiMatch -> multiMatch
								.query(query)
								.fields(fields)
								.type(TextQueryType.BoolPrefix)
						))
						.minimumShouldMatch("1")
				))
				.build();
	}

	private TicketResponse toTicketResponse(TicketDocument document) {
		UserResponse customer = new UserResponse(
				document.getCustomerId(),
				null,
				null,
				document.getCustomerEmail(),
				null,
				null,
				null
		);
		UserResponse assignedAgent = document.getAssignedAgentId() == null
				? null
				: new UserResponse(document.getAssignedAgentId(), null, null, null, null, null, null);

		return new TicketResponse(
				Long.valueOf(document.getId()),
				document.getTitle(),
				document.getDescription(),
				document.getStatus(),
				document.getPriority(),
				customer,
				assignedAgent,
				document.getCreatedAt(),
				document.getUpdatedAt()
		);
	}

	private UserResponse toUserResponse(UserDocument document) {
		return new UserResponse(
				Long.valueOf(document.getId()),
				document.getFirstName(),
				document.getLastName(),
				document.getEmail(),
				document.getRole(),
				null,
				null
		);
	}
}
