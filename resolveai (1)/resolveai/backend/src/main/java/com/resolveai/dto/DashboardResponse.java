package com.resolveai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalComplaints;
    private long openComplaints;
    private long assignedComplaints;
    private long inProgressComplaints;
    private long escalatedComplaints;
    private long resolvedComplaints;
    private long closedComplaints;
    private Double averageResolutionTimeHours; // null if no resolved complaints yet
    private Map<String, Long> complaintsByCategory;
    private Map<String, Long> complaintsByDepartment;
    private List<ComplaintResponse> recentComplaints;
    private List<ComplaintResponse> recentEscalations;
    private Double averageFeedbackRating; // null if no feedback yet
    private long totalFeedbackCount;
}
