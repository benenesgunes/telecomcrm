package com.enes.telecomcrm.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.enes.telecomcrm.search.document.UserDocument;

public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, String> {
}
