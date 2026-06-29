package com.back.baton.domain.chat.integration;

import com.back.baton.domain.chat.dto.request.ChatMessageCreateReq;
import com.back.baton.domain.chat.entity.ChatMessage;
import com.back.baton.domain.chat.entity.ChatRoom;
import com.back.baton.domain.chat.repository.ChatMessageRepository;
import com.back.baton.domain.chat.repository.ChatRoomRepository;
import com.back.baton.support.security.WithMockSecurityUser;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 채팅 메시지 전송/조회 API의 실제 계층 연결을 검증하는 통합 테스트입니다.
@SpringBootTest(properties = "jwt.secret=chat-message-integration-test-secret-key")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ChatMessageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    @DisplayName("채팅방 참여자는 메시지를 전송한 뒤 메시지 목록에서 조회할 수 있다")
    @WithMockSecurityUser(userId = 10)
    void sendMessageAndGetMessages_success() throws Exception {
        Long talentId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        String content = "통합 테스트 메시지입니다.";

        ChatRoom chatRoom = chatRoomRepository.saveAndFlush(
                ChatRoom.createForMatch(
                        talentId,
                        buyerId,
                        sellerId
                )
        );

        ChatMessageCreateReq req = new ChatMessageCreateReq(content);

        mockMvc.perform(post("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-11"))
                .andExpect(jsonPath("$.message").value("채팅 메시지 전송에 성공했습니다."))
                .andExpect(jsonPath("$.data.roomId").value(chatRoom.getId()))
                .andExpect(jsonPath("$.data.senderId").value(buyerId))
                .andExpect(jsonPath("$.data.messageType").value("TEXT"))
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.read").value(false));

        Long cursor = null;
        int size = 20;

        List<ChatMessage> savedMessages = chatMessageRepository.findMessages(
                chatRoom.getId(),
                cursor,
                size
        );

        assertThat(savedMessages).hasSize(1);
        assertThat(savedMessages.get(0).getChatRoom().getId()).isEqualTo(chatRoom.getId());
        assertThat(savedMessages.get(0).getSenderId()).isEqualTo(buyerId);
        assertThat(savedMessages.get(0).getContent()).isEqualTo(content);
        assertThat(savedMessages.get(0).isRead()).isFalse();

        mockMvc.perform(get("/api/v1/chat-rooms/{chatRoomId}/messages", chatRoom.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-12"))
                .andExpect(jsonPath("$.message").value("채팅 메시지 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].roomId").value(chatRoom.getId()))
                .andExpect(jsonPath("$.data.content[0].senderId").value(buyerId))
                .andExpect(jsonPath("$.data.content[0].messageType").value("TEXT"))
                .andExpect(jsonPath("$.data.content[0].content").value(content))
                .andExpect(jsonPath("$.data.content[0].read").value(false))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(savedMessages.get(0).getId()));
    }
}