package com.enes.telecomcrm.ticket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.enes.telecomcrm.ticket.entity.Ticket;
import com.enes.telecomcrm.ticket.entity.TicketPriority;
import com.enes.telecomcrm.ticket.entity.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	List<Ticket> findByCustomerId(Long customerId);

	List<Ticket> findByAssignedAgentId(Long assignedAgentId);

	List<Ticket> findByStatus(TicketStatus status);

	List<Ticket> findByPriority(TicketPriority priority);

	long countByStatus(TicketStatus status);

	long countByAssignedAgentId(Long assignedAgentId);

	long countByAssignedAgentIdAndStatus(Long assignedAgentId, TicketStatus status);

	boolean existsByIdAndCustomerId(Long id, Long customerId);

	@Query("""
			select t.status as status,
			       count(t.id) as ticketCount
			from Ticket t
			group by t.status
			""")
	List<TicketStatusCountView> countByStatusDistribution();

	@Query("""
			select t.priority as priority,
			       count(t.id) as ticketCount
			from Ticket t
			group by t.priority
			""")
	List<TicketPriorityCountView> countByPriorityDistribution();

	@Query(value = """
			select coalesce(avg(extract(epoch from (updated_at - created_at)) / 3600), 0)
			from tickets
			where assigned_agent_id = :agentId
			  and status in ('RESOLVED', 'CLOSED')
			""", nativeQuery = true)
	double averageResolutionTimeHoursByAssignedAgentId(@Param("agentId") Long agentId);

	interface TicketStatusCountView {

		TicketStatus getStatus();

		long getTicketCount();
	}

	interface TicketPriorityCountView {

		TicketPriority getPriority();

		long getTicketCount();
	}
}
