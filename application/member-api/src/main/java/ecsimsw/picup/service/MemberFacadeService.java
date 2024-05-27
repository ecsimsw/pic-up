package ecsimsw.picup.service;

import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.domain.AuthTokens;
import ecsimsw.picup.dto.MemberInfo;
import ecsimsw.picup.dto.MemberResponse;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
import feign.FeignException;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberFacadeService {

    private static final int STORAGE_SERVER_REQUEST_TOKEN_TIMEOUT_SEC = 5;

    private final MemberService memberService;
    private final AuthTokenService authTokenService;
    private final StorageUsageClient storageUsageClient;

    public MemberResponse signUp(SignUpRequest signUpRequest, HttpServletResponse response) {
        var member = memberService.signUp(signUpRequest);
        var tokens = issueAuthToken(response, member);
        var usage = storageUsageClient.init(tokens.getAccessToken());
        return MemberResponse.of(member, usage);
    }

    public MemberResponse signIn(SignInRequest request, HttpServletResponse response) {
        var member = memberService.signIn(request);
        var tokens = issueAuthToken(response, member);
        var usage = storageUsageClient.getUsage(tokens.getAccessToken());
        return MemberResponse.of(member, usage);
    }

    public MemberResponse me(long userId) {
        var member = memberService.me(userId);
        var payload = new TokenPayload(member.id(), member.username());
        var accessToken = authTokenService.createToken(payload, STORAGE_SERVER_REQUEST_TOKEN_TIMEOUT_SEC);
        var usage = storageUsageClient.getUsage(accessToken);
        return MemberResponse.of(member, usage);
    }

    public void delete(long userId) {
        var member = memberService.me(userId);
        var payload = new TokenPayload(member.id(), member.username());
        var accessToken = authTokenService.createToken(payload, STORAGE_SERVER_REQUEST_TOKEN_TIMEOUT_SEC);
        storageUsageClient.deleteAll(accessToken);
        memberService.delete(userId);
    }

    private AuthTokens issueAuthToken(HttpServletResponse response, MemberInfo member) {
        var tokens = authTokenService.issue(new TokenPayload(member.id(), member.username()));
        response.addCookie(authTokenService.accessTokenCookie(tokens));
        response.addCookie(authTokenService.refreshTokenCookie(tokens));
        return tokens;
    }
}
