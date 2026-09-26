package com.resolveai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resolveai.entity.AIAnalysisStatus;
import com.resolveai.entity.Priority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Real AI provider implementation, calling an OpenAI-compatible chat completions API.
 *
 * ACTIVATION:
 *   1. Set the environment variable AI_API_KEY (never hardcode it / never commit it).
 *   2. Set AI_PROVIDER=openai (defaults to "mock" otherwise).
 *   3. Optionally set AI_API_URL if you use a different OpenAI-compatible endpoint.
 *
 * See the README section "AI API setup" for exact steps.
 *
 * All failure modes (timeout, network error, malformed JSON, empty response) are
 * caught here and turned into an AIAnalysisResult with a FAILED/TIMEOUT/UNAVAILABLE
 * status - they NEVER propagate as exceptions that could break complaint creation.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public class AIServiceOpenAIImpl implements AIService {

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.api-url}")
    private String apiUrl;

    @Value("${app.ai.timeout-ms}")
    private long timeoutMs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebClient client() {
        return WebClient.builder().build();
    }

    @Override
    public AIAnalysisResult analyzeComplaint(String title, String description) {
        if (apiKey == null || apiKey.isBlank()) {
            return AIAnalysisResult.unavailable("AI_API_KEY is not configured");
        }
        if (title == null || description == null || (title + description).isBlank()) {
            return AIAnalysisResult.failed("Empty complaint text - nothing to analyze");
        }

        String prompt = """
                You are a complaint triage assistant. Given the complaint title and description,
                respond with STRICT JSON only, no extra text, in exactly this shape:
                {"category": "...", "subCategory": "...", "priority": "LOW|MEDIUM|HIGH|CRITICAL", "summary": "..."}

                Title: %s
                Description: %s
                """.formatted(title, description);

        try {
            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "temperature", 0.2
            );

            String rawResponse = client().post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            return parseResponse(rawResponse);

        } catch (java.util.concurrent.TimeoutException te) {
            log.warn("AI request timed out");
            return AIAnalysisResult.timeout();
        } catch (Exception e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                log.warn("AI request timed out");
                return AIAnalysisResult.timeout();
            }
            log.warn("AI request failed: {}", e.getMessage());
            return AIAnalysisResult.failed("AI service error: " + e.getMessage());
        }
    }

    private AIAnalysisResult parseResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return AIAnalysisResult.failed("AI returned an empty response");
        }
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String content = root.path("choices").path(0).path("message").path("content").asText();

            if (content.isBlank()) {
                return AIAnalysisResult.failed("AI response had no content");
            }

            JsonNode parsed = objectMapper.readTree(content);

            String category = parsed.path("category").asText(null);
            String subCategory = parsed.path("subCategory").asText(null);
            String summary = parsed.path("summary").asText(null);
            String priorityStr = parsed.path("priority").asText("MEDIUM");

            if (category == null || summary == null) {
                return AIAnalysisResult.failed("AI response was missing required fields");
            }

            Priority priority;
            try {
                priority = Priority.valueOf(priorityStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                priority = Priority.MEDIUM; // defensive default for malformed priority
            }

            return AIAnalysisResult.builder()
                    .status(AIAnalysisStatus.SUCCESS)
                    .category(category)
                    .subCategory(subCategory)
                    .priority(priority)
                    .summary(summary)
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON: {}", e.getMessage());
            return AIAnalysisResult.failed("AI returned a malformed/unparseable response");
        }
    }

    @Override
    public String suggestResolution(String complaintText, String similarResolvedText) {
        if (apiKey == null || apiKey.isBlank()) {
            return "AI is not configured - no suggestion available. Please investigate manually.";
        }
        try {
            String prompt = "Given this complaint: \"" + complaintText + "\" and this similar previously "
                    + "resolved complaint's resolution: \"" + similarResolvedText + "\", suggest a brief, "
                    + "practical next investigation step in one or two sentences.";

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "temperature", 0.3
            );

            String rawResponse = client().post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            JsonNode root = objectMapper.readTree(rawResponse);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            return content.isBlank() ? "No suggestion could be generated." : content;

        } catch (Exception e) {
            log.warn("AI resolution suggestion failed: {}", e.getMessage());
            return "AI suggestion unavailable right now - please investigate manually.";
        }
    }
}
