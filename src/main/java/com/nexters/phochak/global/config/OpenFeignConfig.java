package com.nexters.phochak.global.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("com.nexters.phochak.client")
public class OpenFeignConfig {
}
