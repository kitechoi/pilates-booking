package com.pilaslot.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String memberNumber,
        @NotBlank String password
) {
}
