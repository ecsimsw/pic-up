package ecsimsw.mymarket.controller;

import ecsimsw.mymarket.auth.resolver.LoginUser;
import ecsimsw.mymarket.auth.resolver.LoginUserInfo;
import ecsimsw.mymarket.dto.SignInRequest;
import ecsimsw.mymarket.dto.SignUpRequest;
import ecsimsw.mymarket.service.MemberService;
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

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/api/auth/signin")
    public ResponseEntity<Void> signIn(@RequestBody @Valid SignInRequest request,
                                       HttpServletResponse response) {
        memberService.signIn(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/auth/signup")
    public ResponseEntity<Void> signUp(@RequestBody @Valid SignUpRequest request,
                                       HttpServletResponse response) {
        memberService.signUp(request, response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<String> me(@LoginUser LoginUserInfo userInfo) {
        System.out.println(userInfo.getUsername());
        System.out.println(userInfo.getId());
        return ResponseEntity.ok(userInfo.getUsername());
    }
}
