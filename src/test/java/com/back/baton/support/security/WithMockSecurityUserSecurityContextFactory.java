package com.back.baton.support.security;

import com.back.baton.global.security.SecurityUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockSecurityUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockSecurityUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockSecurityUser annotation) {
        SecurityUser principal = SecurityUser.of(annotation.userId(), annotation.role());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                "",
                principal.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
