package com.nexters.phochak.auth.presentation.api;

import com.nexters.phochak.common.DocumentGenerator;
import com.nexters.phochak.common.Scenario;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LoginApi {
    private String provider = "kakao";
    private String token = "testCode";

    public LoginApi withProvider(final String provider) {
        this.provider = provider;
        return this;
    }

    public LoginApi withToken(final String token) {
        this.token = token;
        return this;
    }

    public Scenario request(final MockMvc mockMvc) throws Exception {
        process(mockMvc);
        return new Scenario();
    }

    public Scenario requestAndCreateDocument(final MockMvc mockMvc) throws Exception {
        final ResultActions response = process(mockMvc);
        DocumentGenerator.loginDocument(response);
        return new Scenario();
    }

    private ResultActions process(final MockMvc mockMvc) throws Exception {
        final ResultActions response = mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/v2/auth/login/{provider}", provider)
                                .param("token", token)
                                .param("fcmDeviceToken", "TestFcmDeviceToken"))
                .andExpect(status().isOk());
        return response;
    }
}
