package com.resolveai.dto;

import com.resolveai.entity.ComplaintHistory;
import com.resolveai.entity.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintHistoryResponse {
    private ComplaintStatus fromStatus;
    private ComplaintStatus toStatus;
    private String changedByName;
    private String note;
    private LocalDateTime changedAt;

    public static ComplaintHistoryResponse from(ComplaintHistory h) {
        return ComplaintHistoryResponse.builder()
                .fromStatus(h.getFromStatus())
                .toStatus(h.getToStatus())
                .changedByName(h.getChangedBy() != null ? h.getChangedBy().getName() : "System")
                .note(h.getNote())
                .changedAt(h.getChangedAt())
                .build();
    }
}
