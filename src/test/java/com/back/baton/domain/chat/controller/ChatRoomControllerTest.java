package com.back.baton.domain.chat.controller;

import com.back.baton.domain.chat.dto.request.ChatRoomCreateReq;
import com.back.baton.domain.chat.dto.response.ChatRoomRes;
import com.back.baton.domain.chat.entity.ChatRoomType;
import com.back.baton.domain.chat.service.ChatService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.ChatErrorCode;
import com.back.baton.global.response.code.TalentErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import com.back.baton.support.security.WithMockSecurityUser;
import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatRoomControllerTest {

    private static final long CURRENT_USER_ID = 10L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockSecurityUser(userId = CURRENT_USER_ID)
    @DisplayName("채팅방 생성 또는 조회 API - 성공")
    void getOrCreateChatRoom_success() throws Exception {
        Long talentId = 1L;
        Long currentUserId = CURRENT_USER_ID;
        Long sellerId = 20L;

        ChatRoomCreateReq req = new ChatRoomCreateReq(talentId);

        ChatRoomRes res = new ChatRoomRes(
                100L,
                talentId,
                currentUserId,
                sellerId,
                null,
                null,
                ChatRoomType.MATCH,
                null,
                LocalDateTime.now()
        );

        given(chatService.getOrCreateMatchRoom(eq(talentId), eq(currentUserId)))
                .willReturn(res);

        mockMvc.perform(post("/api/v1/chat-rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-10"))
                .andExpect(jsonPath("$.message").value("채팅방 생성 또는 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.talentId").value(talentId))
                .andExpect(jsonPath("$.data.buyerId").value(currentUserId))
                .andExpect(jsonPath("$.data.sellerId").value(sellerId))
                .andExpect(jsonPath("$.data.status").value("MATCH"));

        then(chatService).should().getOrCreateMatchRoom(talentId, currentUserId);
    }

    @Test
    @WithMockSecurityUser(userId = CURRENT_USER_ID)
    @DisplayName("채팅방 생성 또는 조회 API - talentId가 없으면 400 Bad Request를 반환한다")
    void getOrCreateChatRoom_missingTalentId() throws Exception {
        ChatRoomCreateReq req = new ChatRoomCreateReq(null);

        mockMvc.perform(post("/api/v1/chat-rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        then(chatService).shouldHaveNoInteractions();
    }

    @Test
    @WithMockSecurityUser(userId = CURRENT_USER_ID)
    @DisplayName("채팅방 생성 또는 조회 API - 존재하지 않는 재능이면 404를 반환한다")
    void getOrCreateChatRoom_talentNotFound() throws Exception {
        Long talentId = 999L;
        Long currentUserId = CURRENT_USER_ID;

        ChatRoomCreateReq req = new ChatRoomCreateReq(talentId);

        willThrow(new CustomException(TalentErrorCode.TALENT_NOT_FOUND))
                .given(chatService).getOrCreateMatchRoom(talentId, currentUserId);

        mockMvc.perform(post("/api/v1/chat-rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TALENT-404-001"));

        then(chatService).should().getOrCreateMatchRoom(talentId, currentUserId);
    }

    @Test
    @WithMockSecurityUser(userId = CURRENT_USER_ID)
    @DisplayName("채팅방 생성 또는 조회 API - 자기 자신의 재능이면 400을 반환한다")
    void getOrCreateChatRoom_selfChatNotAllowed() throws Exception {
        Long talentId = 1L;
        Long currentUserId = CURRENT_USER_ID;

        ChatRoomCreateReq req = new ChatRoomCreateReq(talentId);

        willThrow(new CustomException(ChatErrorCode.SELF_CHAT_NOT_ALLOWED))
                .given(chatService).getOrCreateMatchRoom(talentId, currentUserId);

        mockMvc.perform(post("/api/v1/chat-rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CHAT-400-001"));

        then(chatService).should().getOrCreateMatchRoom(talentId, currentUserId);
    }
}