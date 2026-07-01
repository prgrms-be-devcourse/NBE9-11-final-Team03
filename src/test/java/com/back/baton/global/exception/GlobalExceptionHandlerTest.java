package com.back.baton.global.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.back.baton.global.response.code.CommonErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("5xx CustomException은 ERROR 로그로 기록된다(Sentry 전송 대상)")
    void handleCustomException_5xx_logsError() {
        handler.handleCustomException(new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR));

        assertThat(appender.list).anyMatch(e -> e.getLevel() == Level.ERROR);
    }

    @Test
    @DisplayName("4xx CustomException은 ERROR 로그로 기록되지 않는다(노이즈 방지)")
    void handleCustomException_4xx_doesNotLogError() {
        handler.handleCustomException(new CustomException(CommonErrorCode.INVALID_INPUT_VALUE));

        assertThat(appender.list).noneMatch(e -> e.getLevel() == Level.ERROR);
    }
}
