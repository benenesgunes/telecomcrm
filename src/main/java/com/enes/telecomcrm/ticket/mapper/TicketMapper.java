package com.enes.telecomcrm.ticket.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.enes.telecomcrm.ticket.dto.TicketAssignRequest;
import com.enes.telecomcrm.ticket.dto.TicketRequest;
import com.enes.telecomcrm.ticket.dto.TicketResponse;
import com.enes.telecomcrm.ticket.entity.Ticket;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface TicketMapper {

	TicketResponse toResponse(Ticket ticket);

	List<TicketResponse> toResponseList(List<Ticket> tickets);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "customer", ignore = true)
	@Mapping(target = "assignedAgent", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "comments", ignore = true)
	Ticket toEntity(TicketRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", ignore = true)
	@Mapping(target = "description", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "priority", ignore = true)
	@Mapping(target = "customer", ignore = true)
	@Mapping(target = "assignedAgent", source = "agentId")
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "comments", ignore = true)
	Ticket toEntity(TicketAssignRequest request);

	default User userFromId(Long id) {
		if (id == null) {
			return null;
		}
		User user = new User();
		user.setId(id);
		return user;
	}
}
