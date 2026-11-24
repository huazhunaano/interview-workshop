package cn.edu.zjut.interview.dto;

import cn.edu.zjut.interview.domain.WorldbookBook;

import java.time.LocalDateTime;

/**
 * 世界书列表项（issue #1 的 {@code GET /api/worldbooks}）。
 *
 * @param entryCount 启用中的条目数；停用条目不计入，避免「有 100 条但注入 0 条」看不出来
 */
public record WorldbookView(
        Long id,
        String code,
        String name,
        String jobCode,
        String description,
        long entryCount,
        LocalDateTime updatedAt) {

    public static WorldbookView of(WorldbookBook book, long entryCount) {
        return new WorldbookView(
                book.getId(),
                book.getCode(),
                book.getName(),
                book.getJobCode(),
                book.getDescription(),
                entryCount,
                book.getUpdatedAt());
    }
}
