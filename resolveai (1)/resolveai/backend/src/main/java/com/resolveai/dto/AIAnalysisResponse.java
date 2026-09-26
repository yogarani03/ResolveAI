package com.resolveai.dto;

import com.resolveai.entity.AIAnalysis;
import com.resolveai.entity.AIAnalysisStatus;
import com.resolveai.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResponse {
    private String suggestedCategory;
    private String suggestedSubCategory;
    private Priority suggestedPriority;
    private String summary;
    private AIAnalysisStatus status;
    private String errorMessage;

    public static AIAnalysisResponse from(AIAnalysis a) {
        return AIAnalysisResponse.builder()
                .suggestedCategory(a.getSuggestedCategory())
                .suggestedSubCategory(a.getSuggestedSubCategory())
                .suggestedPriority(a.getSuggestedPriority())
                .summary(a.getSummary())
                .status(a.getStatus())
                .errorMessage(a.getErrorMessage())
                .build();
    }
}
