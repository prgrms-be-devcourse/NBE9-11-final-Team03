package com.back.baton.domain.talent.dto.response;

import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TalentDetailRes(
        Long id,
        Long categoryId,
        String categoryName,
        String title,
        String content,
        Integer estimatedHours,
        Integer creditPrice,
        TalentStatus status,
        int viewCount,
        int completeCount,
        BigDecimal avgRating,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        AuthorInfo author
) {
    public record AuthorInfo(
            Long authorId,
            String nickname,
            String profileImageUrl,
            String introduction,
            BigDecimal trustScore
    ) {
        public static AuthorInfo from(User author) {
            return new AuthorInfo(
                    author.getId(),
                    author.getNickname(),
                    author.getProfileImageUrl(),
                    author.getIntroduction(),
                    author.getTrustScore()
            );
        }
    }

    // viewCount는 증가전 값
    public static TalentDetailRes from(Talent talent, User author) {
        return new TalentDetailRes(
                talent.getId(),
                talent.getCategory().getId(),
                talent.getCategory().getName(),
                talent.getTitle(),
                talent.getContent(),
                talent.getEstimatedHours(),
                talent.getCreditPrice(),
                talent.getStatus(),
                talent.getViewCount(),
                talent.getCompleteCount(),
                talent.getAvgRating(),
                talent.getCreatedAt(),
                talent.getUpdatedAt(),
                AuthorInfo.from(author)
        );
    }
}