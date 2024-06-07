package ecsimsw.picup.service;

import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.dto.MemberInfo;
import ecsimsw.picup.dto.MemberResponse;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberFacadeService {

    private static final int STORAGE_SERVER_REQUEST_TOKEN_TIMEOUT_SEC = 5;

    private final MemberService memberService;
    private final AuthTokenService authTokenService;
    private final StorageUsageClient storageUsageClient;

    public MemberResponse signUp(SignUpRequest request) {
        var member = memberService.signUp(request.username(), request.password());
        return MemberResponse.of(member);
    }

    public MemberResponse signIn(SignInRequest request) {
        var member = memberService.signIn(request.username(), request.password());
        var accessToken = tempAccessToken(member);
        var usage = storageUsageClient.getUsage(accessToken);
        return MemberResponse.of(member, usage);
    }

    public MemberResponse me(long userId) {
        var member = memberService.me(userId);
        var accessToken = tempAccessToken(member);
        var usage = storageUsageClient.getUsage(accessToken);
        return MemberResponse.of(member, usage);
    }

    public void delete(long userId) {
        memberService.delete(userId);
    }

    private String tempAccessToken(MemberInfo member) {
        var payload = new TokenPayload(member.id(), member.username());
        return authTokenService.createToken(payload, STORAGE_SERVER_REQUEST_TOKEN_TIMEOUT_SEC);
    }
}
