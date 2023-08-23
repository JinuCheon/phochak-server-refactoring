package com.nexters.phochak.auth.presentation;

import com.nexters.phochak.auth.KakaoUserInformation;
import com.nexters.phochak.common.DocumentGenerator;
import com.nexters.phochak.common.RestDocsApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class AuthControllerTest extends RestDocsApiTest {

    @Autowired AuthController authController;
    @MockBean KakaoInformationFeignClient kakaoInformationFeignClient;
    MockMvc mockMvc;

    @BeforeEach
    void setUpMock(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = getMockMvcBuilder(restDocumentation, authController).build();
    }

    @Test
    @DisplayName("인증 API - 로그인 / 회원가입 성공")
    void login() throws Exception {
        String provider = "kakao";
        String token = "testCode";
        final KakaoUserInformation kakaoRequestResponse = new KakaoUserInformation(
                "providerId",
                "connectedAt",
                "nickname",
                "profileImage",
                "thumbnailImage",
                "kakaoAccount"
        );
        when(kakaoInformationFeignClient.call(any(), any())).thenReturn(kakaoRequestResponse);

        final ResultActions response = mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/v2/auth/login/{provider}", provider)
                                .param("token", token)
                                .param("fcmDeviceToken", "TestFcmDeviceToken"))
                .andExpect(status().isOk());
        DocumentGenerator.loginDocument(response);
    }


}