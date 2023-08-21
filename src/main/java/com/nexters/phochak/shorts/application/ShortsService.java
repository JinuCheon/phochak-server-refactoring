package com.nexters.phochak.shorts.application;

import com.nexters.phochak.domain.Post;
import com.nexters.phochak.dto.EncodingCallbackRequestDto;

public interface ShortsService {

    void connectShorts(String key, Post post);

    void processPost(EncodingCallbackRequestDto encodingCallbackRequestDto);
}
