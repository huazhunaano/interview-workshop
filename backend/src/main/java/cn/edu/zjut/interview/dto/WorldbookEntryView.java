package cn.edu.zjut.interview.dto;

import java.util.List;

/**
 * 世界书条目视图（issue #1）。
 *
 * @param activationType always / keyword
 * @param keys           keyword 激活的关键词；always 为空列表
 * @param content        注入到提示词里的正文
 */
public record WorldbookEntryView(
        String entryId,
        String name,
        boolean enabled,
        int priority,
        String activationType,
        List<String> keys,
        String content) {
}
