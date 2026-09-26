package com.resolveai.ai;

import com.resolveai.entity.AIAnalysisStatus;
import com.resolveai.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Plain result object returned by any AIService implementation - completely
 *  decoupled from JPA entities so provider-specific code never touches the DB layer. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResult {
    private AIAnalysisStatus status;
    private String category;
    private String subCategory;
    private Priority priority;
    private String summary;
    private String errorMessage;

    public static AIAnalysisResult unavailable(String reason) {
        return AIAnalysisResult.builder()
                .status(AIAnalysisStatus.UNAVAILABLE)
                .errorMessage(reason)
                .build();
    }

    public static AIAnalysisResult failed(String reason) {
        return AIAnalysisResult.builder()
                .status(AIAnalysisStatus.FAILED)
                .errorMessage(reason)
                .build();
    }

    public static AIAnalysisResult timeout() {
        return AIAnalysisResult.builder()
                .status(AIAnalysisStatus.TIMEOUT)
                .errorMessage("The AI service did not respond in time")
                .build();
    }
}
