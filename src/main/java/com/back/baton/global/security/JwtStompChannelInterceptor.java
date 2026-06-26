package com.back.baton.global.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");

        if (authorization == null) {
            authorization = accessor.getFirstNativeHeader("authorization");
        }

        DecodedJWT decodedJWT = jwtTokenProvider.resolveToken(authorization);

        if (decodedJWT == null) {
            throw new CustomException(AuthErrorCode.UNAUTHORIZED);
        }

        Long userId = getUserId(decodedJWT);
        String role = getRole(decodedJWT);

        SecurityUser principal = SecurityUser.of(userId, role);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        accessor.setUser(authentication);
    }

    private Long getUserId(DecodedJWT decodedJWT) {
        try {
            return Long.valueOf(decodedJWT.getSubject());
        } catch (Exception e) {
            throw new CustomException(AuthErrorCode.UNAUTHORIZED);
        }
    }

    private String getRole(DecodedJWT decodedJWT) {
        String role = decodedJWT.getClaim("role").asString();

        if (role == null || role.isBlank()) {
            throw new CustomException(AuthErrorCode.UNAUTHORIZED);
        }

        return role;
    }
}