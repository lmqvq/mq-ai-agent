package com.mq.mqaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ClassName：KeepAppVectorStoreConfig
 * Package:com.mq.mqaiagent.rag
 * Description: 向量转换和存储
 * Author：MQQQ
 *
 * @Create:2025/6/20 - 15:51
 * @Version:v1.0
 */
@Configuration
public class KeepAppVectorStoreConfig  {

    @Resource
    private KeepAppDocumentLoader keepAppDocumentLoader;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();
        // 加载文档
        List<Document> documents = keepAppDocumentLoader.loadMarkdowns();
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }
}