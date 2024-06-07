package ecsimsw.picup.integration;

import static ecsimsw.picup.config.AuthTokenConfig.ACCESS_TOKEN_COOKIE_NAME;
import static ecsimsw.picup.utils.MemberFixture.USER_NAME;
import static ecsimsw.picup.utils.MemberFixture.USER_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecsimsw.picup.domain.Member;
import ecsimsw.picup.domain.Password;
import ecsimsw.picup.dto.MemberResponse;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.StorageUsageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("사용자 삭제 시나리오 검증")
public class DeleteScenarioTest extends IntegrationApiTestContext {

    private final StorageUsageResponse usageResponse = new StorageUsageResponse(Long.MAX_VALUE, 0);
    private final SignInRequest signInRequest = new SignInRequest(USER_NAME, USER_PASSWORD);

    @BeforeEach
    public void signUpDummy() {
        // given
        var member = new Member(signInRequest.username(), Password.encryptFrom(signInRequest.password()));
        memberRepository.save(member);
        when(storageUsageClient.getUsage(any()))
            .thenReturn(usageResponse);
    }

    @DisplayName("회원 정보를 삭제한다.")
    @Test
    public void deleteMember() throws Exception {
        // when
        var loginResponse = mockMvc.perform(post("/api/member/signin")
            .content(OBJECT_MAPPER.writeValueAsString(signInRequest))
            .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        var userInfo = OBJECT_MAPPER.readValue(loginResponse.getContentAsString(), MemberResponse.class);
        var accessToken = loginResponse.getCookie(ACCESS_TOKEN_COOKIE_NAME);

        var response = mockMvc.perform(delete("/api/member/me")
            .content(OBJECT_MAPPER.writeValueAsString(signInRequest))
            .cookie(accessToken)
            .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertAll(
            () -> assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(memberRepository.existsById(userInfo.id())).isFalse()
        );
    }

    @DisplayName("로그인 토큰없이 회원 정보를 삭제 요청시 401을 응답한다.")
    @Test
    public void deleteMemberWithoutToken() throws Exception {
        // when
        mockMvc.perform(delete("/api/member/me")
            .content(OBJECT_MAPPER.writeValueAsString(signInRequest))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @DisplayName("삭제 요청 성공시 유저 삭제 이벤트가 발행된다.")
    @Test
    public void deleteMemberEvent() throws Exception {
        // when
        var loginResponse = mockMvc.perform(post("/api/member/signin")
            .content(OBJECT_MAPPER.writeValueAsString(signInRequest))
            .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        var userInfo = OBJECT_MAPPER.readValue(loginResponse.getContentAsString(), MemberResponse.class);
        var accessToken = loginResponse.getCookie(ACCESS_TOKEN_COOKIE_NAME);

        mockMvc.perform(delete("/api/member/me")
            .content(OBJECT_MAPPER.writeValueAsString(signInRequest))
            .cookie(accessToken)
            .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        var memberEvent = memberEventRepository.findByUserId(userInfo.id()).orElseThrow();
        assertAll(
            () -> assertThat(memberEvent.isDeletionEvent()).isTrue(),
            () -> assertThat(memberEvent.isSignUpEvent()).isFalse(),
            () -> assertThat(memberEvent.getUserId()).isEqualTo(userInfo.id())
        );
    }
}
