package com.enes.telecomcrm.auth.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.enes.telecomcrm.user.repository.UserRepository;

@Service
public class TelecomCrmUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public TelecomCrmUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		return userRepository.findByEmail(username.toLowerCase())
				.map(UserPrincipal::from)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}
