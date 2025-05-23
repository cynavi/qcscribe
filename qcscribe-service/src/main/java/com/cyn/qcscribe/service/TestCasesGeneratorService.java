package com.cyn.qcscribe.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestCasesGeneratorService {

    private static final String PROMPT_TEMPLATE = """
            You are an experienced QA engineer.
            Feature: {title}
            Acceptance Criteria: {criteria}
            
            Reference Documentation Context:
            {context}
            
            Please generate a comprehensive list of test cases covering positive, negative, boundary, and edge scenarios, each formatted as "- [ID] Description".
            """;
    private final KnowledgeService knowledgeService;
    private final ChatClient chatClient;

    public TestCasesGeneratorService(KnowledgeService knowledgeService, ChatClient chatClient) {
        this.knowledgeService = knowledgeService;
        this.chatClient = chatClient;
    }

    public List<String> generateTestCases(String title, List<String> acceptanceCriteria) {
        String query = title + "\n" + String.join("\n", acceptanceCriteria);
        List<String> contexts = knowledgeService.retrieveRelevant(query);
        String contextSection = String.join("\n---\n", contexts);
        Prompt prompt = PromptTemplate.builder()
                .template(PROMPT_TEMPLATE)
                .build()
                .create(Map.of("title", title, "criteria", acceptanceCriteria, "context", contextSection));
        String result = chatClient.prompt(prompt).call().content();
        return result == null ? List.of() : Arrays.stream(result.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
