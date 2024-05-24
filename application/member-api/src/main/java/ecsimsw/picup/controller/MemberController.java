package ecsimsw.picup.controller;

import ecsimsw.picup.annotation.LoginUser;
import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.dto.MemberResponse;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
import ecsimsw.picup.service.MemberFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberFacadeService memberFacadeService;

    @PostMapping("/api/member/signin")
    public ResponseEntity<MemberResponse> signIn(
        @Valid @RequestBody SignInRequest signInRequest,
        HttpServletResponse response
    ) {
        var memberInfo = memberFacadeService.signIn(signInRequest, response);
        return ResponseEntity.ok(memberInfo);
    }

    @PostMapping("/api/member/signup")
    public ResponseEntity<MemberResponse> signUp(
        @Valid @RequestBody SignUpRequest signUpRequest,
        HttpServletResponse response
    ) {
        var memberInfo = memberFacadeService.signUp(signUpRequest, response);
        return ResponseEntity.ok(memberInfo);
    }

    @GetMapping("/api/member/me")
    public ResponseEntity<MemberResponse> me(@LoginUser TokenPayload user) {
        var memberInfo = memberFacadeService.me(user.id());
        return ResponseEntity.ok(memberInfo);
    }

    @DeleteMapping("/api/member/me")
    public ResponseEntity<MemberResponse> delete(@LoginUser TokenPayload user) {
        memberFacadeService.delete(user.id());
        return ResponseEntity.ok().build();
    }
}
