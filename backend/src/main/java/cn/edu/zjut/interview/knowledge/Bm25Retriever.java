package cn.edu.zjut.interview.knowledge;

import cn.edu.zjut.interview.dto.KnowledgeHit;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 纯 BM25 检索实现（issue #2）。
 *
 * <p>和 {@link RetrievalService#retrieve} 的区别：这里只走 BM25，不调用 embedding 服务。
 * 断网、没有 embedding key、或者调试时想排除向量路，都可以直接用这个实现；
 * 它复用同一份索引与范围解析，所以「按文档范围」的语义与混合检索完全一致。
 *
 * <p>阈值/权重沿用 {@link RetrievalService.SearchOptions#defaults()}：
 * 调试页需要看未过滤候选时会走 Controller 的阈值覆盖分支，而不是改这里。
 */
@Component
public class Bm25Retriever implements Retriever {

    private final RetrievalService retrievalService;

    public Bm25Retriever(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @Override
    public List<KnowledgeHit> retrieve(String query, List<String> docCodes, int topK) {
        return retrievalService.searchByDocCodes(query, docCodes,
                RetrievalService.SearchOptions.defaults().withTopK(topK),
                RetrievalService.RetrievalMode.BM25_ONLY);
    }
}
