package ecsimsw.picup.album.controller;

import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.AuthTokenService;
import ecsimsw.picup.auth.TokenPayload;
import ecsimsw.picup.album.dto.MemberInfoResponse;
import ecsimsw.picup.album.dto.SignInRequest;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;
    private final AuthTokenService authTokenService;

    @PostMapping("/api/member/signin")
    public ResponseEntity<MemberInfoResponse> signIn(
        @Valid @RequestBody SignInRequest request,
        HttpServletResponse response
    ) {
        var memberInfo = memberService.signIn(request);
        var tokens = authTokenService.issue(memberInfo.toTokenPayload());
        response.addHeader(HttpHeaders.SET_COOKIE, authTokenService.accessTokenCookie(tokens).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authTokenService.refreshTokenCookie(tokens).toString());
        return ResponseEntity.ok(memberInfo);
    }

    @PostMapping("/api/member/signup")
    public ResponseEntity<MemberInfoResponse> signUp(
        @Valid @RequestBody SignUpRequest request,
        HttpServletResponse response
    ) {
        var memberInfo = memberService.signUp(request);
        var tokens = authTokenService.issue(memberInfo.toTokenPayload());
        response.addHeader(HttpHeaders.SET_COOKIE, authTokenService.accessTokenCookie(tokens).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authTokenService.refreshTokenCookie(tokens).toString());
        return ResponseEntity.ok(memberInfo);
    }

    @GetMapping("/api/member/me")
    public ResponseEntity<MemberInfoResponse> me(
        @TokenPayload AuthTokenPayload userInfo
    ) {
        var memberInfo = memberService.me(userInfo.username());
        return ResponseEntity.ok(memberInfo);
    }
}
