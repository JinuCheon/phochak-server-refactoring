package com.nexters.phochak.notification.application;

import com.nexters.phochak.specification.ShortsStateEnum;
import com.nexters.phochak.user.domain.User;

public interface NotificationService {
    void registryFcmDeviceToken(User user, String fcmDeviceToken);

    void postEncodeState(String uploadKey, ShortsStateEnum shortsStateEnum);
}
