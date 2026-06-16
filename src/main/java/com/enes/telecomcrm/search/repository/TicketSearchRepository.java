package com.enes.telecomcrm.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.enes.telecomcrm.search.document.TicketDocument;

public interface TicketSearchRepository extends ElasticsearchRepository<TicketDocument, String> {
}
