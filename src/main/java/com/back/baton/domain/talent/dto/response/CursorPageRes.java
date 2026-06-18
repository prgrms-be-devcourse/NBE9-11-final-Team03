package com.back.baton.domain.talent.dto.response;

import java.util.List;

public record CursorPageRes<T>(
        List<T> content,
        boolean hasNext,
        Long nextCursor
) {
    public static <T> CursorPageRes<T> of(List<T> content, boolean hasNext, Long nextCursor) {
        return new CursorPageRes<>(content, hasNext, nextCursor);
    }
}