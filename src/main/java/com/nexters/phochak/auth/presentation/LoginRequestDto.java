package com.nexters.phochak.auth.presentation;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank String token) {
}
