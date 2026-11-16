package cn.edu.zjut.interview.dto;

import cn.edu.zjut.interview.domain.InterviewQuestion;
import cn.edu.zjut.interview.question.QuestionType;

import java.util.List;

/**
 * 题库的一道题（issue #1）：字段与前端原来的 {@code Question} 形状对齐，
 * 前端把 {@code typeLabel} 直接填进 {@code type}、{@code text/reference/followUps} 原样使用，
 * 不需要再写一层映射文案。
 *
 * @param typeCode  英文题型（technical / project / scenario / behavior），过滤参数用它
 * @param typeLabel 中文题型（技术知识 / 项目深挖 / 场景题 / 行为题），展示用它
 * @param text      题干
 * @param reference 参考答案
 */
public record QuestionView(
        Long id,
        String code,
        String jobCode,
        String kpCode,
        String typeCode,
        String typeLabel,
        String difficulty,
        String text,
        String reference,
        List<String> followUps) {

    public static QuestionView of(InterviewQuestion question, List<String> followUps) {
        return new QuestionView(
                question.getId(),
                question.getCode(),
                question.getJobCode(),
                question.getKpCode(),
                question.getTypeCode(),
                QuestionType.labelOf(question.getTypeCode()),
                question.getDifficulty(),
                question.getQuestionText(),
                question.getReferenceAnswer(),
                followUps);
    }
}
