package com.back.baton.domain.chat.repository;

import com.back.baton.domain.chat.entity.ChatRoom;
import com.back.baton.domain.chat.entity.ChatRoomType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {

    @Query("""
            select cr
            from ChatRoom cr
            where cr.talentId = :talentId
              and cr.buyerId = :buyerId
              and cr.sellerId = :sellerId
              and cr.status = :status
              and cr.deletedAt is null
            """)
    Optional<ChatRoom> findActiveRoom(
            @Param("talentId") Long talentId,
            @Param("buyerId") Long buyerId,
            @Param("sellerId") Long sellerId,
            @Param("status") ChatRoomType status
    );

    @Query("""
        select cr
        from ChatRoom cr
        where cr.tradeId = :tradeId
          and cr.deletedAt is null
        """)
    Optional<ChatRoom> findActiveTransactionRoomByTradeId(
            @Param("tradeId") Long tradeId
    );
}