package com.resolveai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResolveRequest {
    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;
}
