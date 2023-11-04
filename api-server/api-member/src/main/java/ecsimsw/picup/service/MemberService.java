package ecsimsw.picup.service;

import ecsimsw.picup.auth.domain.AuthTokens;
import ecsimsw.picup.auth.service.AuthTokenService;
import ecsimsw.picup.auth.service.TokenCookieUtils;
import ecsimsw.picup.domain.Member;
import ecsimsw.picup.domain.MemberRepository;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
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
        responseAuthTokens(response, member);
    }

    @Transactional
    public void signUp(SignUpRequest request, HttpServletResponse response) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("duplicated username");
        }
        final Member member = memberRepository.save(request.toEntity());
        responseAuthTokens(response, member);
    }

    private void responseAuthTokens(HttpServletResponse response, Member member) {
        final AuthTokens authTokens = authTokenService.issueAuthTokens(member.getId(), member.getUsername());
        final List<Cookie> cookies = TokenCookieUtils.createAuthCookies(authTokens);
        for (var cookie : cookies) {
            response.addCookie(cookie);
        }
    }
}
