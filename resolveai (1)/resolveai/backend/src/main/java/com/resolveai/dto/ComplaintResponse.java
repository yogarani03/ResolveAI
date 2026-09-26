package com.resolveai.dto;

import com.resolveai.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** What we send back to the frontend for a complaint - flattened so we never
 *  accidentally expose full entity graphs (and avoid Hibernate lazy-loading issues). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {
    private Long id;
    private String title;
    private String description;
    private String referenceNumber;
    private ComplaintStatus status;
    private Priority priority;

    private Long userId;
    private String userName;

    private Long categoryId;
    private String categoryName;
    private String subCategoryName;

    private Long departmentId;
    private String departmentName;

    private Long assignedStaffId;
    private String assignedStaffName;

    private LocalDateTime dueAt;
    private LocalDateTime escalatedAt;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private AIAnalysisResponse aiAnalysis; // null if not yet analyzed

    public static ComplaintResponse from(Complaint c) {
        return ComplaintResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .referenceNumber(c.getReferenceNumber())
                .status(c.getStatus())
                .priority(c.getPriority())
                .userId(c.getUser().getId())
                .userName(c.getUser().getName())
                .categoryId(c.getCategory() != null ? c.getCategory().getId() : null)
                .categoryName(c.getCategory() != null ? c.getCategory().getName() : null)
                .subCategoryName(c.getCategory() != null ? c.getCategory().getSubCategory() : null)
                .departmentId(c.getDepartment() != null ? c.getDepartment().getId() : null)
                .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : null)
                .assignedStaffId(c.getAssignedStaff() != null ? c.getAssignedStaff().getId() : null)
                .assignedStaffName(c.getAssignedStaff() != null ? c.getAssignedStaff().getName() : null)
                .dueAt(c.getDueAt())
                .escalatedAt(c.getEscalatedAt())
                .resolvedAt(c.getResolvedAt())
                .resolutionNotes(c.getResolutionNotes())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
