package com.enes.telecomcrm.ticket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enes.telecomcrm.ticket.entity.TicketComment;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

	List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

	List<TicketComment> findByAuthorId(Long authorId);

	long countByTicketId(Long ticketId);
}
