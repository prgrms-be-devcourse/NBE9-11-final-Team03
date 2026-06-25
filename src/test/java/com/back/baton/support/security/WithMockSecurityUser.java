package com.back.baton.support.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockSecurityUserSecurityContextFactory.class)
public @interface WithMockSecurityUser {

    long userId();

    String role() default "USER";
}
