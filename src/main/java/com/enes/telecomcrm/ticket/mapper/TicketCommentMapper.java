package com.enes.telecomcrm.ticket.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.enes.telecomcrm.ticket.dto.TicketCommentRequest;
import com.enes.telecomcrm.ticket.dto.TicketCommentResponse;
import com.enes.telecomcrm.ticket.entity.TicketComment;
import com.enes.telecomcrm.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface TicketCommentMapper {

	@Mapping(target = "ticketId", source = "ticket.id")
	TicketCommentResponse toResponse(TicketComment ticketComment);

	List<TicketCommentResponse> toResponseList(List<TicketComment> ticketComments);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "author", ignore = true)
	@Mapping(target = "ticket", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	TicketComment toEntity(TicketCommentRequest request);
}
