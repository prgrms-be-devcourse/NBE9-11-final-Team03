package com.back.baton.global.security;

import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.AuthErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Principal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class JwtStompChannelInterceptorTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtStompChannelInterceptor interceptor;
    private MessageChannel messageChannel;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", "test-secret-key");

        interceptor = new JwtStompChannelInterceptor(jwtTokenProvider);
        messageChannel = mock(MessageChannel.class);
    }

    @Test
    @DisplayName("STOMP CONNECT 요청에 유효한 JWT가 있으면 Principal을 설정한다")
    void connectWithValidJwtSetsPrincipal() {
        String accessToken = jwtTokenProvider.createAccessToken(12L, "USER", new Date());

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer " + accessToken);
        accessor.setLeaveMutable(true);

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        interceptor.preSend(message, messageChannel);

        Principal principal = accessor.getUser();

        assertThat(principal).isNotNull();
        assertThat(principal.getName()).isEqualTo("12");
    }

    @Test
    @DisplayName("STOMP CONNECT 요청에 Authorization 헤더가 없으면 인증 실패 예외를 던진다")
    void connectWithoutAuthorizationThrowsUnauthorized() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(CustomException.class)
                .satisfies(error -> {
                    CustomException exception = (CustomException) error;
                    assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.UNAUTHORIZED);
                });
    }

    @Test
    @DisplayName("STOMP CONNECT 요청에 잘못된 JWT가 있으면 인증 실패 예외를 던진다")
    void connectWithInvalidJwtThrowsUnauthorized() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer invalid-token");
        accessor.setLeaveMutable(true);

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(CustomException.class)
                .satisfies(error -> {
                    CustomException exception = (CustomException) error;
                    assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.UNAUTHORIZED);
                });
    }
}