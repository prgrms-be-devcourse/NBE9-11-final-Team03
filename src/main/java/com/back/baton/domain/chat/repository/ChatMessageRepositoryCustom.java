package com.back.baton.domain.chat.repository;

import com.back.baton.domain.chat.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepositoryCustom {
    List<ChatMessage> findMessages(Long roomId, Long cursor, int size);
}
