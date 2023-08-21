package com.nexters.phochak.notification.domain;

import com.nexters.phochak.common.domain.BaseTime;
import com.nexters.phochak.domain.User;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Getter
@Entity
public class FcmDeviceToken extends BaseTime {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="FCM_DEVICE_TOKEN_ID")
    private Long id;

    @JoinColumn(name = "USER_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private String token;

    @Builder
    public FcmDeviceToken(Long id, User user, String token) {
        this.id = id;
        this.user = user;
        this.token = token;
    }

    public FcmDeviceToken() {
    }

    public void updateDeviceToken(String token) {
        this.token = token;
    }
}