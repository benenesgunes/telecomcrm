package com.enes.telecomcrm.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.enes.telecomcrm.search.document.UserDocument;
import com.enes.telecomcrm.search.repository.UserSearchRepository;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;

class UserSearchIndexServiceTest {

	private final UserSearchRepository userSearchRepository = mock(UserSearchRepository.class);
	private final UserSearchIndexService userSearchIndexService = new UserSearchIndexService(userSearchRepository);

	@Test
	void index_savesUserDocumentWithSearchFields() {
		User user = User.builder()
				.id(1L)
				.firstName("John")
				.lastName("Doe")
				.email("john@example.com")
				.password("secret")
				.role(Role.ROLE_USER)
				.build();

		userSearchIndexService.index(user);

		ArgumentCaptor<UserDocument> documentCaptor = ArgumentCaptor.forClass(UserDocument.class);
		verify(userSearchRepository).save(documentCaptor.capture());
		UserDocument document = documentCaptor.getValue();
		assertThat(document.getId()).isEqualTo("1");
		assertThat(document.getFirstName()).isEqualTo("John");
		assertThat(document.getLastName()).isEqualTo("Doe");
		assertThat(document.getEmail()).isEqualTo("john@example.com");
		assertThat(document.getRole()).isEqualTo("ROLE_USER");
	}

	@Test
	void index_whenElasticsearchFailsDoesNotThrow() {
		User user = User.builder()
				.id(1L)
				.firstName("John")
				.lastName("Doe")
				.email("john@example.com")
				.password("secret")
				.role(Role.ROLE_USER)
				.build();
		when(userSearchRepository.save(any(UserDocument.class))).thenThrow(new RuntimeException("Elasticsearch down"));

		assertDoesNotThrow(() -> userSearchIndexService.index(user));
	}

	@Test
	void delete_whenElasticsearchFailsDoesNotThrow() {
		doThrow(new RuntimeException("Elasticsearch down")).when(userSearchRepository).deleteById("1");

		assertDoesNotThrow(() -> userSearchIndexService.delete(1L));
	}
}
