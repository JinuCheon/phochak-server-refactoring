package com.nexters.phochak.notification.application;

import com.nexters.phochak.domain.User;
import com.nexters.phochak.specification.ShortsStateEnum;

public interface NotificationService {
    void registryFcmDeviceToken(User user, String fcmDeviceToken);

    void postEncodeState(String uploadKey, ShortsStateEnum shortsStateEnum);
}
