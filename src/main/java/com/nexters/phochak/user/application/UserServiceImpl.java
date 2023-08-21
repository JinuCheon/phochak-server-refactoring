package com.nexters.phochak.user.application;

import com.nexters.phochak.auth.OAuthUserInformation;
import com.nexters.phochak.auth.UserContext;
import com.nexters.phochak.auth.application.OAuthService;
import com.nexters.phochak.common.exception.PhochakException;
import com.nexters.phochak.common.exception.ResCode;
import com.nexters.phochak.ignore.IgnoredUserResponseDto;
import com.nexters.phochak.ignore.domain.IgnoredUserRepository;
import com.nexters.phochak.ignore.domain.IgnoredUsers;
import com.nexters.phochak.ignore.domain.IgnoredUsersRelation;
import com.nexters.phochak.notification.application.NotificationService;
import com.nexters.phochak.post.application.PostService;
import com.nexters.phochak.user.UserCheckResponseDto;
import com.nexters.phochak.user.UserInfoResponseDto;
import com.nexters.phochak.user.domain.OAuthProviderEnum;
import com.nexters.phochak.user.domain.User;
import com.nexters.phochak.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final IgnoredUserRepository ignoredUserRepository;
    private static final String NICKNAME_PREFIX = "여행자#";
    private final Map<OAuthProviderEnum, OAuthService> oAuthServiceMap;
    private final UserRepository userRepository;
    private final PostService postService;
    private final NotificationService notificationService;

    @Override
    public Long login(String provider, String code) {
        OAuthProviderEnum providerEnum = OAuthProviderEnum.codeOf(provider);
        OAuthService oAuthService = oAuthServiceMap.get(providerEnum);

        OAuthUserInformation userInformation = oAuthService.requestUserInformation(code);

        User user = getOrCreateUser(userInformation);

        return user.getId();
    }

    @Override
    public Long login(String provider, String code, String fcmDeviceToken) {
        OAuthProviderEnum providerEnum = OAuthProviderEnum.codeOf(provider);
        OAuthService oAuthService = oAuthServiceMap.get(providerEnum);

        OAuthUserInformation userInformation = oAuthService.requestUserInformation(code);

        User user = getOrCreateUser(userInformation);

        notificationService.registryFcmDeviceToken(user, fcmDeviceToken);
        return user.getId();
    }

    @Override
    public void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new PhochakException(ResCode.NOT_FOUND_USER);
        }
    }

    @Override
    public UserCheckResponseDto checkNicknameIsDuplicated(String nickname) {
        return UserCheckResponseDto.of(isDuplicatedNickname(nickname));
    }


    @Override
    public void modifyNickname(String nickname) {
        Long userId = UserContext.CONTEXT.get();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PhochakException(ResCode.NOT_FOUND_USER));

        if (isDuplicatedNickname(nickname)) {
            throw new PhochakException(ResCode.DUPLICATED_NICKNAME);
        }

        user.modifyNickname(nickname);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponseDto getInfo(Long pageOwnerId, Long userId) {
        User pageOwner;
        Boolean isIgnored = false;
        if (pageOwnerId == null) {
            pageOwner = userRepository.findById(userId).orElseThrow(() -> new PhochakException(ResCode.NOT_FOUND_USER));
        } else {
            pageOwner = userRepository.findById(pageOwnerId).orElseThrow(() -> new PhochakException(ResCode.NOT_FOUND_USER));
            User user = userRepository.getReferenceById(userId);
            IgnoredUsersRelation ignoredUsersRelation = IgnoredUsersRelation.builder()
                    .user(user)
                    .ignoredUser(pageOwner)
                    .build();
            isIgnored = ignoredUserRepository.existsByIgnoredUsersRelation(ignoredUsersRelation);
        }
        return UserInfoResponseDto.of(pageOwner, pageOwner.getId().equals(userId), isIgnored);
    }

    @Override
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new PhochakException(ResCode.NOT_FOUND_USER));
        user.withdrawInformation();
        postService.deleteAllPostByUser(user);
    }

    @Override
    public void ignoreUser(Long me, Long ignoredUserId) {
        User user = userRepository.getReferenceById(me);
        User pageOwner = userRepository.findById(ignoredUserId).orElseThrow(() -> new PhochakException(ResCode.NOT_FOUND_USER));
        try {
            IgnoredUsersRelation ignoredUsersRelation = IgnoredUsersRelation.builder()
                    .user(user)
                    .ignoredUser(pageOwner)
                    .build();
            IgnoredUsers ignoredUsers = IgnoredUsers.builder()
                    .ignoredUsersRelation(ignoredUsersRelation)
                    .build();
            ignoredUserRepository.save(ignoredUsers);
        } catch (
                DataIntegrityViolationException e) {
            throw new PhochakException(ResCode.ALREADY_IGNORED_USER);
        }
    }

    @Override
    public void cancelIgnoreUser(Long me, Long ignoredUserId) {
        ignoredUserRepository.deleteIgnore(me, ignoredUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<IgnoredUserResponseDto> getIgnoreUserList(Long me) {
        List<IgnoredUsers> ignoreUserListByUserId = ignoredUserRepository.getIgnoreUserListByUserId(me);
        return IgnoredUserResponseDto.of(ignoreUserListByUserId);
    }

    private boolean isDuplicatedNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    private User getOrCreateUser(OAuthUserInformation userInformation) {
        User user = null;
        Optional<User> target = userRepository.findByProviderAndProviderId(userInformation.getProvider(), userInformation.getProviderId());

        if (target.isPresent()) {
            user = target.orElseThrow(() -> new PhochakException(ResCode.NOT_FOUND_USER));
            log.info("UserServiceImpl|login(기존 회원): {}", userInformation);

        } else {
            log.info("UserServiceImpl|login(신규 회원): {}", userInformation);
            String nickname = generateInitialNickname();

            User newUser = User.builder()
                    .provider(userInformation.getProvider())
                    .providerId(userInformation.getProviderId())
                    .nickname(nickname)
                    .profileImgUrl(userInformation.getInitialProfileImage())
                    .build();

            user = userRepository.save(newUser);
        }
        return user;
    }

    private static String generateInitialNickname() {
        // 초기 닉네임 여행자#난수 6자로 결정
        return NICKNAME_PREFIX + generateUUID();
    }

    private static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, User.NICKNAME_MAX_SIZE - NICKNAME_PREFIX.length());
    }
}