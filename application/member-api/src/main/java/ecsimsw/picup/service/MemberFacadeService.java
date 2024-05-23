package ecsimsw.picup.service;

import ecsimsw.picup.domain.AuthToken;
import ecsimsw.picup.domain.AuthTokens;
import ecsimsw.picup.dto.MemberInfo;
import ecsimsw.picup.dto.MemberResponse;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberFacadeService {

    private final MemberService memberService;
    private final AuthTokenService authTokenService;
    private final StorageUsageClient storageUsageClient;

    public MemberResponse signUp(SignUpRequest signUpRequest, HttpServletResponse response) {
        var member = memberService.signUp(signUpRequest);
        var tokens = issueAuthToken(response, member);
        try {
            var usage = storageUsageClient.init(tokens.getAccessToken());
            return MemberResponse.of(member, usage);
        } catch (Exception e) {
            memberService.delete(member.id());
            throw e;
        }
    }

    public MemberResponse signIn(SignInRequest request, HttpServletResponse response) {
        var member = memberService.signIn(request);
        var tokens = issueAuthToken(response, member);
        var usage = storageUsageClient.getUsage(tokens.getAccessToken());
        return MemberResponse.of(member, usage);
    }

    public MemberResponse me(long userId, HttpServletResponse response) {
        var member = memberService.me(userId);
        var tokens = issueAuthToken(response, member);
        var usage = storageUsageClient.getUsage(tokens.getAccessToken());
        return MemberResponse.of(member, usage);
    }

    public void delete(Long userId) {
        memberService.delete(userId);
    }

    private AuthTokens issueAuthToken(HttpServletResponse response, MemberInfo member) {
        var tokens = authTokenService.issue(new AuthToken(member.id(), member.username()));
        var at = authTokenService.accessTokenCookie(tokens);
        var rt = authTokenService.refreshTokenCookie(tokens);
        response.addCookie(at);
        response.addCookie(rt);
        return tokens;
    }
}
