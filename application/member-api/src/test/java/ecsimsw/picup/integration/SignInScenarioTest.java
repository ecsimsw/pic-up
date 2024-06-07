package ecsimsw.picup.integration;

import static ecsimsw.picup.config.AuthTokenConfig.ACCESS_TOKEN_COOKIE_NAME;
import static ecsimsw.picup.config.AuthTokenConfig.REFRESH_TOKEN_COOKIE_NAME;
import static ecsimsw.picup.utils.MemberFixture.USER_NAME;
import static ecsimsw.picup.utils.MemberFixture.USER_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecsimsw.picup.domain.Member;
import ecsimsw.picup.domain.Password;
import ecsimsw.picup.dto.MemberInfo;
import ecsimsw.picup.dto.MemberResponse;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
import ecsimsw.picup.dto.StorageUsageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("로그인 시나리오 검증")
public class SignInScenarioTest extends IntegrationApiTestContext {

    private final StorageUsageResponse usageResponse = new StorageUsageResponse(Long.MAX_VALUE, 0);
    private final SignInRequest signInRequest = new SignInRequest(USER_NAME, USER_PASSWORD);
    private MemberInfo memberInfo;

    @BeforeEach
    public void signUpDummy() {
        // given
        var member = new Member(signInRequest.username(), Password.encryptFrom(signInRequest.password()));
        memberRepository.save(member);
        memberInfo = MemberInfo.of(member);
        when(storageUsageClient.getUsage(any()))
            .thenReturn(usageResponse);
    }

    @DisplayName("로그인 정보를 확인한다.")
    @Test
    public void signIn() throws Exception {
        // when
        var response = mockMvc.perform(post("/api/member/signin")
            .content(OBJECT_MAPPER.writeValueAsString(signInRequest))
            .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        var expectedBody = OBJECT_MAPPER.writeValueAsString(MemberResponse.of(memberInfo, usageResponse));
        assertAll(
            () -> assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(response.getContentAsString()).isEqualTo(expectedBody)
        );
    }

    @DisplayName("로그인 성공시 로그인 토큰이 발행된다.")
    @Test
    public void signInResponseTokenCookie() throws Exception {
        // when
        var response = mockMvc.perform(post("/api/member/signin")
            .content(OBJECT_MAPPER.writeValueAsString(signInRequest))
            .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertAll(
            () -> assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME)).isNotNull(),
            () -> assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME)).isNotNull()
        );
    }

    @DisplayName("스토리지 서버에서 사용량을 응답받지 못한다면 로그인은 실패한다.")
    @Test
    public void signInFailedByStorageServer() throws Exception {
        // when
        var response = mockMvc.perform(post("/api/member/signin")
            .content(OBJECT_MAPPER.writeValueAsString(signInRequest))
            .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        var memberInfo = MemberInfo.of(memberRepository.findByUsername(USER_NAME).orElseThrow());
        var expectedBody = OBJECT_MAPPER.writeValueAsString(MemberResponse.of(memberInfo, usageResponse));
        assertAll(
            () -> assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(response.getContentAsString()).isEqualTo(expectedBody)
        );
    }

    @DisplayName("사용자 정보 불일치시 401 응답을 반환한다.")
    @Test
    public void signInFailedResponse() throws Exception {
        // when
        var invalid = new SignUpRequest(USER_NAME, "INVALID_PASSWORD");
        mockMvc.perform(post("/api/member/signin")
            .content(OBJECT_MAPPER.writeValueAsString(invalid))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }
}
