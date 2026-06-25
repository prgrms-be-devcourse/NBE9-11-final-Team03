package com.back.baton.domain.chat.repository;

import com.back.baton.domain.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {

    @Query("""
        select cm.id
        from ChatMessage cm
        where cm.chatRoom.id = :roomId
          and cm.senderId <> :readerId
          and cm.read = false
          and cm.deletedAt is null
        """)
    List<Long> findUnreadMessageIdsFromOtherParticipant(
            @Param("roomId") Long roomId,
            @Param("readerId") Long readerId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ChatMessage cm
        set cm.read = true
        where cm.id in :messageIds
        """)
    void markAsReadByIds(
            @Param("messageIds") List<Long> messageIds
    );
}