//package ecsimsw.picup.presentation;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import ecsimsw.picup.config.*;
//import ecsimsw.picup.controller.GlobalControllerAdvice;
//import ecsimsw.picup.controller.MemberController;
//import ecsimsw.picup.domain.AuthTokens;
//import ecsimsw.picup.domain.AuthToken;
//import ecsimsw.picup.dto.MemberInfo;
//import ecsimsw.picup.dto.MemberResponse;
//import ecsimsw.picup.dto.SignInRequest;
//import ecsimsw.picup.dto.SignUpRequest;
//import ecsimsw.picup.exception.UnauthorizedException;
//import ecsimsw.picup.service.AuthTokenArgumentResolver;
//import ecsimsw.picup.service.AuthTokenService;
//import ecsimsw.picup.service.MemberFacadeService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import javax.servlet.http.Cookie;
//
//import static ecsimsw.picup.config.AuthTokenConfig.ACCESS_TOKEN_COOKIE_NAME;
//import static ecsimsw.picup.config.AuthTokenConfig.REFRESH_TOKEN_COOKIE_NAME;
//import static ecsimsw.picup.utils.MemberFixture.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//class MemberControllerUnitTest {
//
//    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
//
//    static {
//        OBJECT_MAPPER.registerModule(new JavaTimeModule());
//    }
//
//    private final MemberFacadeService memberFacadeService = mock(MemberFacadeService.class);
//    private final AuthTokenService authTokenService = mock(AuthTokenService.class);
//
//    private final MockMvc mockMvc = MockMvcBuilders
//        .standaloneSetup(new MemberController(memberFacadeService))
//        .addInterceptors(new AuthTokenInterceptor(authTokenService))
//        .setCustomArgumentResolvers(new AuthTokenArgumentResolver(authTokenService))
//        .setControllerAdvice(new GlobalControllerAdvice())
//        .build();
//
//    @DisplayName("로그인시 토큰과 함께 사용자 정보를 응답한다.")
//    @Test
//    public void signInTest() throws Exception {
//        var signInRequest = new SignInRequest(USER_NAME, USER_PASSWORD);
//        var memberResponse = new MemberResponse(USER_ID, signInRequest.username(), 1l, 1l);
//        var loginUser = new AuthToken(memberResponse.id(), memberResponse.username());
//        var loginAuthToken = AuthTokens.of(loginUser, ACCESS_TOKEN, REFRESH_TOKEN);
//
//        when(memberFacadeService.signIn(signInRequest))
//            .thenReturn(memberResponse);
//        when(authTokenService.issue(loginUser))
//            .thenReturn(loginAuthToken);
//        when(authTokenService.accessTokenCookie(loginAuthToken))
//            .thenReturn(new Cookie(ACCESS_TOKEN_COOKIE_NAME, ACCESS_TOKEN));
//        when(authTokenService.refreshTokenCookie(any()))
//            .thenReturn(new Cookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN));
//
//        mockMvc.perform(post("/api/member/signin")
//                .cookie(new Cookie(ACCESS_TOKEN_COOKIE_NAME, ACCESS_TOKEN))
//                .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN))
//                .content(OBJECT_MAPPER.writeValueAsString(signInRequest))
//                .contentType(MediaType.APPLICATION_JSON)
//            )
//            .andExpect(status().isOk())
//            .andExpect(cookie().value(ACCESS_TOKEN_COOKIE_NAME, ACCESS_TOKEN))
//            .andExpect(cookie().value(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN))
//            .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(memberResponse)));
//    }
//
//    @DisplayName("로그인 실패시 403 에러를 응답한다.")
//    @Test
//    public void signInTestInvalidLoginInfo() throws Exception {
//        var signInInfo = new SignInRequest(USER_NAME, USER_PASSWORD);
//
//        when(memberService.signIn(signInInfo))
//            .thenThrow(new UnauthorizedException("Invalid member info"));
//
//        mockMvc.perform(post("/api/member/signin")
//                .content(OBJECT_MAPPER.writeValueAsString(signInInfo))
//                .contentType(MediaType.APPLICATION_JSON)
//            )
//            .andExpect(status().isUnauthorized())
//            .andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE_NAME))
//            .andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE_NAME));
//    }
//
//    @DisplayName("회원가입시 토큰과 함께 사용자 정보를 응답한다.")
//    @Test
//    public void signUp() throws Exception {
//        var signUpRequest = new SignUpRequest(USER_NAME, USER_PASSWORD);
//        var memberResponse = new MemberInfo(1L, signUpRequest.username());
//        var loginUser = new AuthToken(memberResponse.id(), memberResponse.username());
//        var loginAuthToken = AuthTokens.of(loginUser, ACCESS_TOKEN, REFRESH_TOKEN);
//
//        when(memberService.signUp(signUpRequest))
//            .thenReturn(memberResponse);
//        when(authTokenService.issue(loginUser))
//            .thenReturn(loginAuthToken);
//        when(authTokenService.accessTokenCookie(loginAuthToken))
//            .thenReturn(new Cookie(ACCESS_TOKEN_COOKIE_NAME, ACCESS_TOKEN));
//        when(authTokenService.refreshTokenCookie(loginAuthToken))
//            .thenReturn(new Cookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN));
//
//        mockMvc.perform(post("/api/member/signup")
//                .content(OBJECT_MAPPER.writeValueAsString(signUpRequest))
//                .contentType(MediaType.APPLICATION_JSON)
//            )
//            .andExpect(status().isOk())
//            .andExpect(cookie().value(ACCESS_TOKEN_COOKIE_NAME, ACCESS_TOKEN))
//            .andExpect(cookie().value(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN))
//            .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(memberResponse)));
//    }
//
//    @DisplayName("사용자 이름을 빈칸으로 회원정보로 가입할 수 없다.")
//    @Test
//    public void signUpWithInvalidUsername() throws Exception {
//        var signUpRequest = new SignUpRequest("", USER_PASSWORD);
//
//        mockMvc.perform(post("/api/member/signup")
//                .content(OBJECT_MAPPER.writeValueAsString(signUpRequest))
//                .contentType(MediaType.APPLICATION_JSON)
//            )
//            .andExpect(status().isBadRequest());
//    }
//
//    @DisplayName("사용자 비밀번호를 빈칸으로 회원정보로 가입할 수 없다.")
//    @Test
//    public void signUpWithInvalidPassword() throws Exception {
//        var signUpRequest = new SignUpRequest(USER_NAME, "");
//
//        mockMvc.perform(post("/api/member/signup")
//                .content(OBJECT_MAPPER.writeValueAsString(signUpRequest))
//                .contentType(MediaType.APPLICATION_JSON)
//            )
//            .andExpect(status().isBadRequest());
//    }
//}