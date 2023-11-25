package ecsimsw.picup.controller;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.auth.service.AuthTokenService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.dto.MemberInfoResponse;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
import ecsimsw.picup.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
public class MemberController {

    private final MemberService memberService;
    private final AuthTokenService<AuthTokenPayload> authTokenService;

    public MemberController(MemberService memberService, AuthTokenService<AuthTokenPayload> authTokenService) {
        this.memberService = memberService;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/api/member/signin")
    public ResponseEntity<MemberInfoResponse> signIn(
        @Valid @RequestBody SignInRequest request,
        HttpServletResponse response
    ) {
        final MemberInfoResponse me = memberService.signIn(request);
        authTokenService.issue(response, me.toTokenPayload());
        return ResponseEntity.ok(me);
    }

    @PostMapping("/api/member/signup")
    public ResponseEntity<MemberInfoResponse> signUp(
        @Valid @RequestBody SignUpRequest request,
        HttpServletResponse response
    ) {
        final MemberInfoResponse me = memberService.signUp(request);
        authTokenService.issue(response, me.toTokenPayload());
        return ResponseEntity.ok(me);
    }

    @GetMapping("/api/member/me")
    public ResponseEntity<MemberInfoResponse> me(
        @JwtPayload AuthTokenPayload userInfo
    ) {
        final MemberInfoResponse me = memberService.me(userInfo.getId());
        return ResponseEntity.ok(me);
    }
}
