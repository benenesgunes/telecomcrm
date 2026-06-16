package com.enes.telecomcrm.auth.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enes.telecomcrm.auth.dto.LoginRequest;
import com.enes.telecomcrm.auth.dto.LoginResponse;
import com.enes.telecomcrm.auth.dto.RegisterRequest;
import com.enes.telecomcrm.auth.security.JwtUtil;
import com.enes.telecomcrm.common.exception.BusinessRuleException;
import com.enes.telecomcrm.search.service.UserSearchIndexService;
import com.enes.telecomcrm.user.dto.UserResponse;
import com.enes.telecomcrm.user.entity.Role;
import com.enes.telecomcrm.user.entity.User;
import com.enes.telecomcrm.user.mapper.UserMapper;
import com.enes.telecomcrm.user.repository.UserRepository;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final UserSearchIndexService userSearchIndexService;

	public AuthService(
			UserRepository userRepository,
			UserMapper userMapper,
			PasswordEncoder passwordEncoder,
			JwtUtil jwtUtil,
			UserSearchIndexService userSearchIndexService
	) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.userSearchIndexService = userSearchIndexService;
	}

	@Transactional
	public UserResponse register(RegisterRequest request) {
		String email = request.email().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new BusinessRuleException("Email is already in use");
		}

		User user = userMapper.toEntity(request);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setRole(Role.ROLE_USER);

		User savedUser = userRepository.save(user);
		userSearchIndexService.index(savedUser);
		return userMapper.toResponse(savedUser);
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email().toLowerCase())
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BadCredentialsException("Invalid email or password");
		}

		String token = jwtUtil.generateToken(user);
		return new LoginResponse(token, "Bearer", jwtUtil.getExpirationSeconds());
	}
}
