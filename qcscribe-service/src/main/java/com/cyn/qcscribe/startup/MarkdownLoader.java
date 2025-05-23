package com.cyn.qcscribe.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Component that loads .mdx markdown files from a specific directory (hoppscotch/),
 * extracts their content by sections (based on markdown headings),
 * chunks the sections into smaller manageable pieces,
 * and inserts them into a VectorStore for semantic search or retrieval-based AI use cases.
 */
@Component
public class MarkdownLoader {
    // Max number of words allowed in one chunk
    private static final int MAX_WORDS = 300;
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownLoader.class);

    private final VectorStore vectorStore;

    public MarkdownLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Initialization method that runs after Spring context setup.
     * It loads all .mdx files from the hoppscotch directory,
     * breaks their content into sections and chunks,
     * and populates the vector store.
     */
//    @PostConstruct
    public void init() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // Load all .mdx markdown files recursively under 'hoppscotch' folder in classpath
        Resource[] resources = resolver.getResources("classpath*:hoppscotch/**/*.mdx");

        for (Resource resource : resources) {
            String content = readResource(resource);

            // Extract sections from markdown (e.g., by headings like #, ##, ###)
            List<String> sections = extractSections(content);

            for (String section : sections) {
                // Chunk section into pieces of at most MAX_WORDS words each
                List<String> chunks = chunkSection(section);
                for (String chunk : chunks) {
                    vectorStore.add(List.of(new Document(chunk)));
                    LOGGER.info("Loaded chunk from file {} into vector store.", resource.getFilename());
                }
            }
        }

        LOGGER.info("All Markdown files loaded into vector store.");
    }

    /**
     * Reads the full content of a markdown resource file using UTF-8 encoding.
     *
     * @param resource Spring resource pointing to a markdown file
     * @return Full content as a single String
     * @throws IOException if file read fails
     */
    private String readResource(Resource resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Extracts logical sections from the markdown content using headings.
     * A section is defined as the content between two headings (e.g., #, ##, or ###).
     *
     * @param content Full markdown file content
     * @return List of section strings
     */
    private List<String> extractSections(String content) {
        List<String> sections = new ArrayList<>();
        Pattern headingPattern = Pattern.compile("^#{1,3} .*", Pattern.MULTILINE);
        Matcher matcher = headingPattern.matcher(content);

        int lastIndex = 0;
        while (matcher.find()) {
            if (lastIndex != matcher.start()) {
                String section = content.substring(lastIndex, matcher.start()).trim();
                if (!section.isEmpty()) {
                    sections.add(section);
                }
            }
            lastIndex = matcher.start();
        }

        // Add the last section after the final heading
        if (lastIndex < content.length()) {
            String lastSection = content.substring(lastIndex).trim();
            if (!lastSection.isEmpty()) {
                sections.add(lastSection);
            }
        }

        return sections;
    }

    /**
     * Chunks a section of markdown into smaller pieces based on paragraph boundaries.
     * Each chunk contains up to MAX_WORDS words. If a paragraph itself is too long,
     * it gets split across multiple chunks.
     * <p>
     * This method ensures that paragraphs are grouped together for better semantic retrieval,
     * while also preventing chunks from being too large for the vector embedding model.
     *
     * @param section Markdown section to be chunked
     * @return List of string chunks
     */
    private List<String> chunkSection(String section) {
        List<String> chunks = new ArrayList<>();
        if (section.isBlank()) return chunks;

        // Paragraphs are separated by 2 or more newlines
        String[] paragraphs = section.split("(?m)\\n{2,}");

        StringBuilder currentChunk = new StringBuilder();
        int wordCount = 0;

        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;

            int paraWordCount = trimmed.split("\\s+").length;

            // If adding this paragraph exceeds MAX_WORDS, store the current chunk and start new
            if (wordCount + paraWordCount > MAX_WORDS && !currentChunk.isEmpty()) {
                chunks.add(currentChunk.toString().trim());
                currentChunk.setLength(0);
                wordCount = 0;
            }

            currentChunk.append(trimmed).append("\n\n");
            wordCount += paraWordCount;
        }

        // Add any leftover content as the final chunk
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}