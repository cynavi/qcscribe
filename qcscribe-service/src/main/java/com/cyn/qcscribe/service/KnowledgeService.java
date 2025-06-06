package com.cyn.qcscribe.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KnowledgeService {

    public static final Logger LOGGER = LoggerFactory.getLogger(KnowledgeService.class);

    private final VectorStore vectorStore;

    @Value("${qcscribe.search.topK}")
    private int topK;

    @Value("${qcscribe.search.threshold}")
    private double threshold;

    public KnowledgeService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<String> retrieveRelevant(String query) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build();
        return Optional.ofNullable(vectorStore.similaritySearch(request))
                .orElse(List.of())
                .stream()
                .filter(Document::isText)
                .map(document -> {
                    LOGGER.info("Building knowledge from: {}", document.getText());
                    return document.getText();
                })
                .toList();
    }
}
