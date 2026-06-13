package com.enes.telecomcrm.auth.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

import com.enes.telecomcrm.user.entity.Role;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithTelecomUserSecurityContextFactory.class)
public @interface WithTelecomUser {

	long id() default 1L;

	String email() default "user@example.com";

	Role role() default Role.ROLE_USER;
}
