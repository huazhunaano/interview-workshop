package cn.edu.zjut.interview.dto;

import java.util.List;

/**
 * 世界书详情（issue #1）：列表项字段 + 全部条目。
 *
 * @param entries 按 sortOrder 排列；enabled=false 的条目也返回，便于看出「哪条被停用了」
 */
public record WorldbookDetailView(
        String code,
        String name,
        String jobCode,
        String description,
        long entryCount,
        List<WorldbookEntryView> entries) {
}
