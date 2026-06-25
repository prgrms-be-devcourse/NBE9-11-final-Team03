package com.back.baton.domain.chat.repository;

import com.back.baton.domain.chat.dto.response.ChatRoomListRes;
import java.util.List;

public interface ChatRoomRepositoryCustom {

    List<ChatRoomListRes> findMyChatRooms(Long userId, Long cursor, int size);
}