package com.nexters.phochak.auth.presentation;

import jakarta.validation.constraints.NotBlank;

public record LoginV2RequestDto(
        @NotBlank String token,
        @NotBlank String fcmDeviceToken) {
}
