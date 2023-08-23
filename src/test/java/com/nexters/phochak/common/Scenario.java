package com.nexters.phochak.common;

import com.nexters.phochak.auth.presentation.api.LoginApi;

public class Scenario {

    public static LoginApi login() {
        return new LoginApi();
    }
}
