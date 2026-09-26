package com.resolveai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedComplaintResponse {
    private Long complaintId;
    private String title;
    private double similarityScore; // 0.0 - 1.0, simple keyword-overlap based score
}
