package com.back.baton.global.response;

import java.util.List;
import java.util.function.Function;

public record CursorPageRes<T>(
        List<T> content,
        boolean hasNext,
        Long nextCursor
) {
    public static <T> CursorPageRes<T> of(List<T> content, boolean hasNext, Long nextCursor) {
        return new CursorPageRes<>(content, hasNext, nextCursor);
    }

    public static <T> CursorPageRes<T> from(List<T> rows, int pageSize, Function<T, Long> cursorExtractor) {
        boolean hasNext = rows.size() > pageSize;
        List<T> content = hasNext ? List.copyOf(rows.subList(0, pageSize)) : rows;
        Long nextCursor = content.isEmpty() ? null : cursorExtractor.apply(content.get(content.size() - 1));
        return new CursorPageRes<>(content, hasNext, nextCursor);
    }
}