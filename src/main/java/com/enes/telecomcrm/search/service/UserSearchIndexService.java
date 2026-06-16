package com.enes.telecomcrm.search.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.enes.telecomcrm.search.document.UserDocument;
import com.enes.telecomcrm.search.repository.UserSearchRepository;
import com.enes.telecomcrm.user.entity.User;

@Service
public class UserSearchIndexService {

	private static final Logger log = LoggerFactory.getLogger(UserSearchIndexService.class);

	private final UserSearchRepository userSearchRepository;

	public UserSearchIndexService(UserSearchRepository userSearchRepository) {
		this.userSearchRepository = userSearchRepository;
	}

	public void index(User user) {
		try {
			userSearchRepository.save(toDocument(user));
		}
		catch (RuntimeException ex) {
			log.warn("Failed to index user id={}", user.getId(), ex);
		}
	}

	public void delete(Long userId) {
		try {
			userSearchRepository.deleteById(String.valueOf(userId));
		}
		catch (RuntimeException ex) {
			log.warn("Failed to remove user id={} from search index", userId, ex);
		}
	}

	private UserDocument toDocument(User user) {
		return UserDocument.builder()
				.id(String.valueOf(user.getId()))
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.email(user.getEmail())
				.role(user.getRole().name())
				.build();
	}
}
