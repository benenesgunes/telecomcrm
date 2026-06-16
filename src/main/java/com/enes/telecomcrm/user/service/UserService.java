package com.enes.telecomcrm.user.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.search.service.UserSearchIndexService;
import com.enes.telecomcrm.user.dto.UserRequest;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.exception.UserNotFoundException;
import com.enes.telecomcrm.user.mapper.UserMapper;
import com.enes.telecomcrm.user.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final UserSearchIndexService userSearchIndexService;

	public UserService(
			UserRepository userRepository,
			UserMapper userMapper,
			PasswordEncoder passwordEncoder,
			UserSearchIndexService userSearchIndexService
	) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
		this.userSearchIndexService = userSearchIndexService;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> getAllUsers() {
		return userMapper.toResponseList(userRepository.findAll());
	}

	@Transactional(readOnly = true)
	public UserResponse getUserById(Long id) {
		return userMapper.toResponse(findUserById(id));
	}

	@Transactional
	public UserResponse updateUser(Long id, UserRequest request) {
		User user = findUserById(id);
		String email = request.email().toLowerCase();

		if (userRepository.existsByEmailAndIdNot(email, id)) {
			throw new BusinessRuleException("Email is already in use");
		}

		user.setFirstName(request.firstName());
		user.setLastName(request.lastName());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.password()));

		User savedUser = userRepository.save(user);
		userSearchIndexService.index(savedUser);
		return userMapper.toResponse(savedUser);
	}

	@Transactional
	public UserResponse deleteUser(Long id) {
		User user = findUserById(id);
		UserResponse response = userMapper.toResponse(user);
		userRepository.delete(user);
		userSearchIndexService.delete(id);
		return response;
	}

	private User findUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));
	}
}
