package com.back.baton.global.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.back.baton.global.security.JwtTokenProvider;
import com.back.baton.global.security.SecurityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 헤더에서 토큰 꺼내서 검증
        DecodedJWT decodedJWT = jwtTokenProvider.resolveToken(request);

        if(decodedJWT != null){
            try{
                // 2. Spring Security용 인증 객체 생성
                Long userId = Long.parseLong(decodedJWT.getSubject());
                String role = decodedJWT.getClaim("role").asString();

                SecurityUser principal = SecurityUser.of(userId, role);
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());

                // 3. SecurityContext에 등록하여 관리
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch(Exception e){
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
