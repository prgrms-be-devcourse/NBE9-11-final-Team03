package com.back.baton.domain.chat.controller;

import com.back.baton.domain.chat.dto.request.ChatMessageCreateReq;
import com.back.baton.domain.chat.dto.response.ChatMessageRes;
import com.back.baton.domain.chat.entity.ChatMessageType;
import com.back.baton.domain.chat.service.ChatService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.ChatErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import com.back.baton.support.security.WithMockSecurityUser;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("채팅 메시지 전송 API - 성공")
    @WithMockSecurityUser(userId = 10)
    void sendMessage_success() throws Exception {
        Long chatRoomId = 1L;
        Long senderId = 10L;
        String content = "안녕하세요";

        ChatMessageCreateReq req = new ChatMessageCreateReq(content);

        ChatMessageRes res = new ChatMessageRes(
                100L,
                chatRoomId,
                senderId,
                ChatMessageType.TEXT,
                content,
                false,
                LocalDateTime.now()
        );

        given(chatService.sendMessage(eq(chatRoomId), eq(senderId), eq(content))).willReturn(res);

        mockMvc.perform(post("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-11"))
                .andExpect(jsonPath("$.message").value("채팅 메시지 전송에 성공했습니다."))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.roomId").value(chatRoomId))
                .andExpect(jsonPath("$.data.senderId").value(senderId))
                .andExpect(jsonPath("$.data.messageType").value("TEXT"))
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.read").value(false));

        then(chatService).should().sendMessage(chatRoomId, senderId, content);
    }

    @Test
    @DisplayName("채팅 메시지 전송 API - 인증 정보가 없으면 401 Unauthorized를 반환한다")
    void sendMessage_unauthorized() throws Exception {
        Long chatRoomId = 1L;
        ChatMessageCreateReq req = new ChatMessageCreateReq("안녕하세요");

        mockMvc.perform(post("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH-401-001"));

        then(chatService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("채팅 메시지 전송 API - content가 비어 있으면 400 Bad Request를 반환한다")
    @WithMockSecurityUser(userId = 10)
    void sendMessage_blankContent() throws Exception {
        Long chatRoomId = 1L;

        ChatMessageCreateReq req = new ChatMessageCreateReq("");

        mockMvc.perform(post("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON-400-002"))
                .andExpect(jsonPath("$.data.content").value("메시지 내용은 필수입니다."));

        then(chatService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("채팅 메시지 전송 API - content가 1000자를 초과하면 400 Bad Request를 반환한다")
    @WithMockSecurityUser(userId = 10)
    void sendMessage_contentTooLong() throws Exception {
        Long chatRoomId = 1L;

        ChatMessageCreateReq req = new ChatMessageCreateReq("a".repeat(1001));

        mockMvc.perform(post("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON-400-002"))
                .andExpect(jsonPath("$.data.content").value("메시지는 1000자 이하로 입력해 주세요."));

        then(chatService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("채팅 메시지 전송 API - 존재하지 않는 채팅방이면 404를 반환한다")
    @WithMockSecurityUser(userId = 10)
    void sendMessage_chatRoomNotFound() throws Exception {
        Long chatRoomId = 999L;
        Long senderId = 10L;
        String content = "안녕하세요";

        ChatMessageCreateReq req = new ChatMessageCreateReq(content);

        willThrow(new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND))
                .given(chatService).sendMessage(chatRoomId, senderId, content);

        mockMvc.perform(post("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CHAT-404-001"));

        then(chatService).should().sendMessage(chatRoomId, senderId, content);
    }

    @Test
    @DisplayName("채팅 메시지 전송 API - 채팅방 참여자가 아니면 403을 반환한다")
    @WithMockSecurityUser(userId = 999)
    void sendMessage_accessDenied() throws Exception {
        Long chatRoomId = 1L;
        Long senderId = 999L;
        String content = "안녕하세요";

        ChatMessageCreateReq req = new ChatMessageCreateReq(content);

        willThrow(new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED))
                .given(chatService).sendMessage(chatRoomId, senderId, content);

        mockMvc.perform(post("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CHAT-403-001"));

        then(chatService).should().sendMessage(chatRoomId, senderId, content);
    }

    @Test
    @DisplayName("채팅 메시지 목록 조회 API - 성공")
    @WithMockSecurityUser(userId = 10)
    void getMessages_success() throws Exception {
        Long chatRoomId = 1L;
        Long userId = 10L;

        List<ChatMessageRes> res = List.of(
                new ChatMessageRes(
                        100L,
                        chatRoomId,
                        10L,
                        ChatMessageType.TEXT,
                        "첫 번째 메시지",
                        false,
                        LocalDateTime.now()
                ),
                new ChatMessageRes(
                        101L,
                        chatRoomId,
                        20L,
                        ChatMessageType.TEXT,
                        "두 번째 메시지",
                        false,
                        LocalDateTime.now()
                )
        );

        given(chatService.getMessages(eq(chatRoomId), eq(userId))).willReturn(res);

        mockMvc.perform(get("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-12"))
                .andExpect(jsonPath("$.message").value("채팅 메시지 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(100L))
                .andExpect(jsonPath("$.data[0].roomId").value(chatRoomId))
                .andExpect(jsonPath("$.data[0].senderId").value(10L))
                .andExpect(jsonPath("$.data[0].messageType").value("TEXT"))
                .andExpect(jsonPath("$.data[0].content").value("첫 번째 메시지"))
                .andExpect(jsonPath("$.data[1].id").value(101L))
                .andExpect(jsonPath("$.data[1].roomId").value(chatRoomId))
                .andExpect(jsonPath("$.data[1].senderId").value(20L))
                .andExpect(jsonPath("$.data[1].messageType").value("TEXT"))
                .andExpect(jsonPath("$.data[1].content").value("두 번째 메시지"));

        then(chatService).should().getMessages(chatRoomId, userId);
    }

    @Test
    @DisplayName("채팅 메시지 목록 조회 API - 인증 정보가 없으면 401 Unauthorized를 반환한다")
    void getMessages_unauthorized() throws Exception {
        Long chatRoomId = 1L;

        mockMvc.perform(get("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoomId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH-401-001"));

        then(chatService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("채팅 메시지 목록 조회 API - 존재하지 않는 채팅방이면 404를 반환한다")
    @WithMockSecurityUser(userId = 10)
    void getMessages_chatRoomNotFound() throws Exception {
        Long chatRoomId = 999L;
        Long userId = 10L;

        willThrow(new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND))
                .given(chatService).getMessages(chatRoomId, userId);

        mockMvc.perform(get("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CHAT-404-001"));

        then(chatService).should().getMessages(chatRoomId, userId);
    }

    @Test
    @DisplayName("채팅 메시지 목록 조회 API - 채팅방 참여자가 아니면 403을 반환한다")
    @WithMockSecurityUser(userId = 999)
    void getMessages_accessDenied() throws Exception {
        Long chatRoomId = 1L;
        Long userId = 999L;

        willThrow(new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED))
                .given(chatService).getMessages(chatRoomId, userId);

        mockMvc.perform(get("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoomId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CHAT-403-001"));

        then(chatService).should().getMessages(chatRoomId, userId);
    }
}