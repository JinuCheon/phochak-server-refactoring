package com.nexters.phochak.auth.application;

import com.nexters.phochak.auth.OAuthUserInformation;
import com.nexters.phochak.auth.presentation.KakaoInformationFeignClient;
import com.nexters.phochak.user.domain.OAuthProviderEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoOAuthServiceImpl implements OAuthService {
    private static final OAuthProviderEnum OAUTH_PROVIDER = OAuthProviderEnum.KAKAO;
    private static final String TOKEN_PREFIX = "Bearer ";
    private final KakaoInformationFeignClient kakaoInformationFeignClient;

    @Override
    public OAuthProviderEnum getOAuthProvider() {
        return OAUTH_PROVIDER;
    }

    @Override
    public OAuthUserInformation requestUserInformation(String token) {
        return kakaoInformationFeignClient.call(
                "application/x-www-form-urlencoded;charset=utf-8",
                TOKEN_PREFIX + token
        );
    }
}
