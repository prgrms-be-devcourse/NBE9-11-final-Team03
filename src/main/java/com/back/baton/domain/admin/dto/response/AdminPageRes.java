package com.back.baton.domain.admin.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record AdminPageRes<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> AdminPageRes<T> from(Page<T> page) {
        return new AdminPageRes<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
