package com.back.baton.global.security;

import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.ErrorCode;

import java.nio.charset.StandardCharsets;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Component
public class JwtStompErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(
            Message<byte[]> clientMessage,
            Throwable ex
    ) {
        CustomException customException = findCustomException(ex);

        if (customException == null) {
            return super.handleClientMessageProcessingError(clientMessage, ex);
        }

        ErrorCode errorCode = customException.getErrorCode();
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(errorCode.getMessage());
        accessor.setContentType(MimeTypeUtils.APPLICATION_JSON);
        accessor.setLeaveMutable(true);

        String payload = """
                {"success":false,"code":"%s","message":"%s","data":null}
                """.formatted(
                escapeJson(errorCode.getCode()),
                escapeJson(errorCode.getMessage())
        );

        return MessageBuilder.createMessage(
                payload.getBytes(StandardCharsets.UTF_8),
                accessor.getMessageHeaders()
        );
    }

    private CustomException findCustomException(Throwable ex) {
        Throwable current = ex;

        while (current != null) {
            if (current instanceof CustomException customException) {
                return customException;
            }

            current = current.getCause();
        }

        return null;
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
