package com.zorvyn.finance.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Reusable pagination wrapper to avoid leaking Spring Data's Page JSON structure.
 */
public record PagedResponse<T>(
        List<T> items,
        PageInfo page
) {

    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        PageInfo meta = new PageInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
        return new PagedResponse<>(page.getContent(), meta);
    }

    public record PageInfo(
            int number,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
    }
}

