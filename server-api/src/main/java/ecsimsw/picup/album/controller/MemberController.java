package ecsimsw.picup.album.controller;

import ecsimsw.picup.auth.LoginUser;
import ecsimsw.picup.auth.AuthTokenService;
import ecsimsw.picup.auth.TokenPayload;
import ecsimsw.picup.album.dto.MemberResponse;
import ecsimsw.picup.album.dto.SignInRequest;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.MemberService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<MemberResponse> signIn(
        @Valid @RequestBody SignInRequest request,
        HttpServletResponse response
    ) {
        var memberInfo = memberService.signIn(request);
        var tokens = authTokenService.issue(memberInfo.toTokenPayload());
        response.addCookie(authTokenService.accessTokenCookie(tokens));
        response.addCookie(authTokenService.refreshTokenCookie(tokens));
        return ResponseEntity.ok(memberInfo);
    }

    @PostMapping("/api/member/signup")
    public ResponseEntity<MemberResponse> signUp(
        @Valid @RequestBody SignUpRequest request,
        HttpServletResponse response
    ) {
        var memberInfo = memberService.signUp(request);
        var tokens = authTokenService.issue(memberInfo.toTokenPayload());
        response.addCookie(authTokenService.accessTokenCookie(tokens));
        response.addCookie(authTokenService.refreshTokenCookie(tokens));
        return ResponseEntity.ok(memberInfo);
    }

    @GetMapping("/api/member/me")
    public ResponseEntity<MemberResponse> me(
        @TokenPayload LoginUser userInfo
    ) {
        var memberInfo = memberService.me(userInfo.username());
        return ResponseEntity.ok(memberInfo);
    }
}
