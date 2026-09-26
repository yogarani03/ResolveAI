package com.resolveai.dto;

import com.resolveai.entity.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull(message = "New status is required")
    private ComplaintStatus status;

    private String note;
}
