package com.resolveai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRequest {
    @NotNull(message = "Staff id is required")
    private Long staffId;
}
