package com.back.baton.domain.chat.repository;

import com.back.baton.domain.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            select cm
            from ChatMessage cm
            where cm.chatRoom.id = :roomId
              and cm.deletedAt is null
            order by cm.createdAt asc
            """)
    List<ChatMessage> findMessages(
            @Param("roomId") Long roomId
    );
}