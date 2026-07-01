package com.back.baton.global.security;

import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.AuthErrorCode;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class JwtStompErrorHandlerTest {

    private final JwtStompErrorHandler errorHandler = new JwtStompErrorHandler();

    @Test
    @DisplayName("STOMP 처리 중 CustomException이 발생하면 구조화된 ERROR 프레임을 반환한다")
    void handleCustomExceptionAsStompErrorFrame() {
        StompHeaderAccessor clientAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<byte[]> clientMessage = MessageBuilder.createMessage(
                new byte[0],
                clientAccessor.getMessageHeaders()
        );

        MessageDeliveryException exception = new MessageDeliveryException(
                clientMessage,
                new CustomException(AuthErrorCode.UNAUTHORIZED)
        );

        Message<byte[]> result = errorHandler.handleClientMessageProcessingError(
                clientMessage,
                exception
        );

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        String payload = new String(result.getPayload(), StandardCharsets.UTF_8);

        assertThat(accessor.getCommand()).isEqualTo(StompCommand.ERROR);
        assertThat(accessor.getMessage()).isEqualTo(AuthErrorCode.UNAUTHORIZED.getMessage());
        assertThat(payload).contains("\"success\":false");
        assertThat(payload).contains(AuthErrorCode.UNAUTHORIZED.getCode());
        assertThat(payload).contains(AuthErrorCode.UNAUTHORIZED.getMessage());
    }
}
