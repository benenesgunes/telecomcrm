package com.enes.telecomcrm.auth.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

class WithTelecomUserSecurityContextFactory implements WithSecurityContextFactory<WithTelecomUser> {

	@Override
	public SecurityContext createSecurityContext(WithTelecomUser annotation) {
		UserPrincipal principal = new UserPrincipal(
				annotation.id(),
				annotation.email(),
				"password",
				annotation.role()
		);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				principal,
				null,
				principal.getAuthorities()
		);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		return context;
	}
}
