package com.enes.telecomcrm.user.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.enes.telecomcrm.auth.dto.RegisterRequest;
import com.enes.telecomcrm.user.dto.UserRequest;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.enes.telecomcrm.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	UserResponse toResponse(User user);

	List<UserResponse> toResponseList(List<User> users);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "subscriptions", ignore = true)
	@Mapping(target = "customerTickets", ignore = true)
	@Mapping(target = "assignedTickets", ignore = true)
	@Mapping(target = "comments", ignore = true)
	User toEntity(UserRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "subscriptions", ignore = true)
	@Mapping(target = "customerTickets", ignore = true)
	@Mapping(target = "assignedTickets", ignore = true)
	@Mapping(target = "comments", ignore = true)
	User toEntity(RegisterRequest request);
}
