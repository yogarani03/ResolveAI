package com.resolveai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Stores AI's SUGGESTED category/priority/summary for a complaint. This is never
 *  treated as authoritative - staff/admin can always override it manually. */
@Entity
@Table(name = "ai_analysis")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AIAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id", nullable = false, unique = true)
    private Complaint complaint;

    @Column(name = "suggested_category", length = 100)
    private String suggestedCategory;

    @Column(name = "suggested_sub_category", length = 100)
    private String suggestedSubCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_priority", length = 20)
    private Priority suggestedPriority;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AIAnalysisStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
