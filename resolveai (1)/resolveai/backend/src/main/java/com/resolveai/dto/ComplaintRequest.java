package com.resolveai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Used when a USER creates a new complaint. */
@Data
public class ComplaintRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private Long categoryId;      // optional - AI will suggest one if omitted
    private Long departmentId;    // optional
    private String referenceNumber; // optional order/reference number
    private String suggestedPriority; // optional - user may suggest, staff/admin decide
}
