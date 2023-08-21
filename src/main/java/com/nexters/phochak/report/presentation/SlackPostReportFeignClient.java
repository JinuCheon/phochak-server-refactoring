package com.nexters.phochak.report.presentation;

import com.nexters.phochak.dto.SlackMessageFormDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "ReportSlackNotificationFeignClient", url = "${slack.report.web-hook-uri}")
public interface SlackPostReportFeignClient {

    @PostMapping
    String call(SlackMessageFormDto slackMessageFormDto);
}
