package cn.edu.zjut.interview.knowledge;

import cn.edu.zjut.interview.dto.KnowledgeHit;

import java.util.List;

/**
 * 检索接口（issue #2）：给一句话、给一组文档、给取几条，返回排好序的片段。
 *
 * <p>为什么要接口而不是直接依赖 {@link RetrievalService}：答题时老师会问「为什么这条排第一」，
 * 将来也可能换向量库或换召回策略。调用方只认这个契约，换实现时不用改注入点。
 *
 * <p>BM25 的实现是 {@link RetrievalService} 的 {@code RetrievalMode.BM25_ONLY}：
 * 两种召回共用同一份索引与范围解析，单独再包一个只有两行委托的类没有收益。
 */
public interface Retriever {

    /**
     * @param query    用户原话（本轮输入，不是上一轮的问题）
     * @param docCodes 只在这些文档内检索；空集合表示范围为空，不回落整库
     * @param topK     最多返回几条，超过 {@link RetrievalService#MAX_TOP_K} 会被截断
     * @return 按相关度降序的片段；没有命中时返回空列表
     */
    List<KnowledgeHit> retrieve(String query, List<String> docCodes, int topK);
}
