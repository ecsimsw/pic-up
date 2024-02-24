package ecsimsw.picup.member.controller;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.auth.service.AuthTokenService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.member.dto.MemberInfoResponse;
import ecsimsw.picup.member.dto.SignInRequest;
import ecsimsw.picup.member.dto.SignUpRequest;
import ecsimsw.picup.member.service.MemberService;
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
    private final AuthTokenService<AuthTokenPayload> authTokenService;

    @PostMapping("/api/member/signin")
    public ResponseEntity<MemberInfoResponse> signIn(
        @Valid @RequestBody SignInRequest request,
        HttpServletResponse response
    ) {
        var me = memberService.signIn(request);
        authTokenService.issue(response, me.toTokenPayload());
        return ResponseEntity.ok(me);
    }

    @PostMapping("/api/member/signup")
    public ResponseEntity<MemberInfoResponse> signUp(
        @Valid @RequestBody SignUpRequest request,
        HttpServletResponse response
    ) {
        var me = memberService.signUp(request);
        authTokenService.issue(response, me.toTokenPayload());
        return ResponseEntity.ok(me);
    }

    @GetMapping("/api/member/me")
    public ResponseEntity<MemberInfoResponse> me(
        @JwtPayload AuthTokenPayload userInfo
    ) {
        var me = memberService.me(userInfo.getId());
        return ResponseEntity.ok(me);
    }
}
