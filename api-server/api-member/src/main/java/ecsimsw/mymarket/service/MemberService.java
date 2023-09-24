package ecsimsw.mymarket.service;

import ecsimsw.mymarket.auth.service.AuthTokenService;
import ecsimsw.mymarket.domain.Member;
import ecsimsw.mymarket.domain.MemberRepository;
import ecsimsw.mymarket.dto.SignInRequest;
import ecsimsw.mymarket.dto.SignUpRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthTokenService authTokenService;

    public MemberService(MemberRepository memberRepository, AuthTokenService authTokenService) {
        this.memberRepository = memberRepository;
        this.authTokenService = authTokenService;
    }

    @Transactional
    public void signIn(SignInRequest request, HttpServletResponse response) {
        final Member member = memberRepository.findByUsernameAndPassword(request.getUsername(), request.getPassword())
            .orElseThrow(() -> new IllegalArgumentException("Invalid login info"));
        final List<Cookie> cookies = authTokenService.issueAuthTokens(member.getId(), member.getUsername());
        for (var cookie : cookies) {
            response.addCookie(cookie);
        }
    }

    @Transactional
    public void signUp(SignUpRequest request, HttpServletResponse response) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("duplicated username");
        }
        final Member member = memberRepository.save(request.toEntity());
        final List<Cookie> cookies = authTokenService.issueAuthTokens(member.getId(), member.getUsername());
        for (var cookie : cookies) {
            response.addCookie(cookie);
        }
    }
}
