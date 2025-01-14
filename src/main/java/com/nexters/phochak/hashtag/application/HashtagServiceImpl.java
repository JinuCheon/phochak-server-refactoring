package com.nexters.phochak.hashtag.application;

import com.nexters.phochak.common.exception.PhochakException;
import com.nexters.phochak.common.exception.ResCode;
import com.nexters.phochak.hashtag.domain.Hashtag;
import com.nexters.phochak.hashtag.domain.HashtagFetchDto;
import com.nexters.phochak.hashtag.domain.HashtagRepository;
import com.nexters.phochak.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HashtagServiceImpl implements HashtagService {

    private final HashtagRepository hashtagRepository;

    @Override
    public List<Hashtag> saveHashtagsByString(List<String> stringHashtagList, Post post) {
        if (stringHashtagList.isEmpty()) {
            return Collections.emptyList();
        }
        validateHashtag(stringHashtagList);
        List<Hashtag> hashtagList = stringHashtagList.stream().map(stringHashtag ->
                Hashtag.builder()
                        .post(post)
                        .tag(stringHashtag)
                        .build()
        ).collect(Collectors.toList());
        return hashtagRepository.saveAll(hashtagList);
    }

    @Override
    public Map<Long, HashtagFetchDto> findHashtagsOfPosts(List<Long> postIds) {
        return hashtagRepository.findHashTagsOfPost(postIds);
    }

    @Override
    public void updateAll(Post post, List<String> stringHashtagList) {
        hashtagRepository.deleteAllByPostId(post.getId());
        saveHashtagsByString(stringHashtagList, post);
    }

    private static void validateHashtag(List<String> stringHashtagList) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣_]{1,20}$");
        for(String tag : stringHashtagList) {
            if (!pattern.matcher(tag).matches()) {
                throw new PhochakException(ResCode.INVALID_INPUT, "해시태그 형식이 올바르지 않습니다.");
            }
        }
    }

}
