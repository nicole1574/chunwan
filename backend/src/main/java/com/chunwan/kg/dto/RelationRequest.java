package com.chunwan.kg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RelationRequest(
        @NotBlank String personId,
        @NotBlank String programId,
        @NotBlank String relationType,
        String roleId,
        String categoryId,
        String yearId
) {
}
