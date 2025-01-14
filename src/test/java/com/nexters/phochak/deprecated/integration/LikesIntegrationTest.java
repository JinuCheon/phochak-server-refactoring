package com.nexters.phochak.deprecated.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexters.phochak.auth.application.JwtTokenService;
import com.nexters.phochak.common.docs.RestDocs;
import com.nexters.phochak.common.exception.PhochakException;
import com.nexters.phochak.common.exception.ResCode;
import com.nexters.phochak.likes.domain.Likes;
import com.nexters.phochak.likes.domain.LikesRepository;
import com.nexters.phochak.likes.presentation.LikesController;
import com.nexters.phochak.post.domain.Post;
import com.nexters.phochak.post.domain.PostCategoryEnum;
import com.nexters.phochak.post.domain.PostRepository;
import com.nexters.phochak.shorts.domain.Shorts;
import com.nexters.phochak.shorts.domain.ShortsRepository;
import com.nexters.phochak.user.domain.OAuthProviderEnum;
import com.nexters.phochak.user.domain.User;
import com.nexters.phochak.user.domain.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.nexters.phochak.auth.aspect.AuthAspect.AUTHORIZATION_HEADER;
import static com.nexters.phochak.common.exception.ResCode.OK;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Transactional
class LikesIntegrationTest extends RestDocs {

    @Autowired UserRepository userRepository;
    @Autowired JwtTokenService jwtTokenService;
    @Autowired ObjectMapper objectMapper;
    MockMvc mockMvc;
    @Autowired
    LikesController likesController;

    static String testToken;
    @Autowired
    private ShortsRepository shortsRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private LikesRepository likesRepository;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = getMockMvcBuilder(restDocumentation, likesController).build();
        User user = User.builder()
                .providerId("1234")
                .provider(OAuthProviderEnum.KAKAO)
                .nickname("nickname")
                .profileImgUrl(null)
                .build();
        userRepository.save(user);
        JwtTokenService.TokenVo tokenDto = jwtTokenService.generateToken(user.getId(), 999999999L);
        testToken = JwtTokenService.TokenVo.TOKEN_TYPE + " " + tokenDto.getTokenString();
    }

    @Test
    @DisplayName("포착하기 요청 API - 포착하기 성공")
    void addPhochak() throws Exception {
        //given
        User user = userRepository.findByNickname("nickname").orElseThrow(() -> new PhochakException(ResCode.NOT_FOUND_USER));
        Shorts shorts = Shorts.builder()
                .uploadKey("key")
                .shortsUrl("shortsUrl")
                .thumbnailUrl("thumbnailUrl")
                .build();
        shortsRepository.save(shorts);

        Post post = Post.builder()
                .user(user)
                .shorts(shorts)
                .postCategory(PostCategoryEnum.TOUR)
                .build();
        postRepository.save(post);

        //when, then
        mockMvc.perform(post("/v1/post/{postId}/likes/", post.getId()).header(AUTHORIZATION_HEADER, testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.resCode").value(OK.getCode()))
                .andDo(document("post/{postId}/likes/post",
                        preprocessRequest(modifyUris().scheme("http").host("101.101.209.228").removePort(), prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("postId").description("(필수) 해당 게시글 Id")
                        ),
                        requestHeaders(
                                headerWithName(AUTHORIZATION_HEADER)
                                        .description("(필수) JWT Access Token")
                        ),
                        responseFields(
                                fieldWithPath("status.resCode").type(JsonFieldType.STRING).description("응답 코드"),
                                fieldWithPath("status.resMessage").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("null")
                        )
                ));
    }

    @Test
    @DisplayName("포착 취소하기 요청 API - 포착 취소하기 성공")
    void cancelPhochak() throws Exception {
        //given
        User user = userRepository.findByNickname("nickname").orElseThrow(() -> new PhochakException(ResCode.NOT_FOUND_USER));
        Shorts shorts = Shorts.builder()
                .uploadKey("key")
                .shortsUrl("shortsUrl")
                .thumbnailUrl("thumbnailUrl")
                .build();
        shortsRepository.save(shorts);

        Post post = Post.builder()
                .user(user)
                .shorts(shorts)
                .postCategory(PostCategoryEnum.TOUR)
                .build();
        postRepository.save(post);

        Likes likes = Likes.builder()
                        .user(user)
                        .post(post)
                        .build();
        likesRepository.save(likes);

        //when, then
        mockMvc.perform(delete("/v1/post/{postId}/likes", post.getId()).header(AUTHORIZATION_HEADER, testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.resCode").value(OK.getCode()))
                .andDo(document("post/{postId}/likes/delete",
                        preprocessRequest(modifyUris().scheme("http").host("101.101.209.228").removePort(), prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("postId").description("(필수) 해당 게시글 Id")
                        ),
                        requestHeaders(
                                headerWithName(AUTHORIZATION_HEADER)
                                        .description("(필수) JWT Access Token")
                        ),
                        responseFields(
                                fieldWithPath("status.resCode").type(JsonFieldType.STRING).description("응답 코드"),
                                fieldWithPath("status.resMessage").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("null")
                        )
                ));

    }

}
