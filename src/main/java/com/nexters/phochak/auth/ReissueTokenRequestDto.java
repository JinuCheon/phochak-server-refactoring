package com.nexters.phochak.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReissueTokenRequestDto {
    private String accessToken;
    private String refreshToken;

}