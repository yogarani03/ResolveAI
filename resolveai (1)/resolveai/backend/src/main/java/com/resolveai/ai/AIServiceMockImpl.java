package com.resolveai.ai;

import com.resolveai.entity.AIAnalysisStatus;
import com.resolveai.entity.Priority;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Offline, deterministic, keyword-based "AI" so the whole application can be run,
 *  tested and demonstrated end-to-end with ZERO external API key.
 *
 *  This is activated by default (app.ai.provider=mock in application.properties, or
 *  simply unset). Set app.ai.provider=openai (and AI_API_KEY) to use
 *  AIServiceOpenAIImpl instead - only one AIService bean is ever created.
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class AIServiceMockImpl implements AIService {

    @Override
    public AIAnalysisResult analyzeComplaint(String title, String description) {
        String text = (title + " " + description).toLowerCase();

        if (text.isBlank()) {
            return AIAnalysisResult.failed("Empty complaint text - nothing to analyze");
        }

        String category;
        String subCategory;
        Priority priority;

        if (containsAny(text, "payment", "refund", "charged", "deducted", "transaction")) {
            category = "PAYMENT";
            subCategory = text.contains("order") ? "PAYMENT_DEDUCTED_ORDER_FAILED" : "PAYMENT_ISSUE";
            priority = Priority.HIGH;
        } else if (containsAny(text, "wifi", "wi-fi", "internet", "network", "connection")) {
            category = "NETWORK";
            subCategory = "CONNECTIVITY_ISSUE";
            priority = Priority.MEDIUM;
        } else if (containsAny(text, "printer", "scanner", "hardware", "device", "machine")) {
            category = "HARDWARE";
            subCategory = "DEVICE_MALFUNCTION";
            priority = Priority.MEDIUM;
        } else if (containsAny(text, "rude", "misbehav", "staff", "harass", "unprofessional")) {
            category = "STAFF_CONDUCT";
            subCategory = "BEHAVIOR_COMPLAINT";
            priority = Priority.HIGH;
        } else if (containsAny(text, "delay", "late", "not delivered", "pending", "waiting")) {
            category = "SERVICE_DELAY";
            subCategory = "PENDING_ACTION";
            priority = Priority.MEDIUM;
        } else if (containsAny(text, "urgent", "emergency", "critical", "immediately")) {
            category = "GENERAL";
            subCategory = "URGENT_ISSUE";
            priority = Priority.CRITICAL;
        } else {
            category = "GENERAL";
            subCategory = "UNCLASSIFIED";
            priority = Priority.LOW;
        }

        String summary = buildSummary(title, description);

        return AIAnalysisResult.builder()
                .status(AIAnalysisStatus.SUCCESS)
                .category(category)
                .subCategory(subCategory)
                .priority(priority)
                .summary(summary)
                .build();
    }

    @Override
    public String suggestResolution(String complaintText, String similarResolvedText) {
        if (similarResolvedText == null || similarResolvedText.isBlank()) {
            return "No closely matching resolved complaint was found. Suggest starting with standard "
                    + "diagnostic steps for this category and documenting findings in the investigation notes.";
        }
        return "Suggestion (based on a similar resolved complaint): " + similarResolvedText
                + ". This is only a suggestion - please verify it applies before using it.";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }

    private String buildSummary(String title, String description) {
        String trimmed = description.length() > 160 ? description.substring(0, 160) + "..." : description;
        return "Summary: " + title + " - " + trimmed;
    }
}
