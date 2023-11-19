package ecsimsw.picup.service;

import ecsimsw.picup.auth.domain.AuthTokens;
import ecsimsw.picup.auth.service.AuthTokenService;
import ecsimsw.picup.auth.service.TokenCookieUtils;
import ecsimsw.picup.domain.Member;
import ecsimsw.picup.domain.MemberRepository;
import ecsimsw.picup.dto.MemberInfoResponse;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
import ecsimsw.picup.exception.LoginFailedException;
import ecsimsw.picup.exception.MemberException;
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
    public MemberInfoResponse signIn(SignInRequest request, HttpServletResponse response) {
        try {
            final Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new LoginFailedException("Invalid login info"));
            member.authenticate(request.getPassword());
            responseAuthTokens(member, response);
            return MemberInfoResponse.of(member);
        } catch (Exception e) {
            throw new LoginFailedException("Invalid login info");
        }
    }

    @Transactional
    public MemberInfoResponse signUp(SignUpRequest request, HttpServletResponse response) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new MemberException("Duplicated username");
        }
        final Member member = request.toEntity();
        memberRepository.save(member);
        responseAuthTokens(member, response);
        return MemberInfoResponse.of(member);
    }

    private void responseAuthTokens(Member member, HttpServletResponse response) {
        final AuthTokens authTokens = authTokenService.issueAuthTokens(member.getId(), member.getUsername());
        final List<Cookie> cookies = TokenCookieUtils.createAuthCookies(authTokens);
        for (var cookie : cookies) {
            response.addCookie(cookie);
        }
    }

    public MemberInfoResponse me(Long id) {
        final Member member = memberRepository.findById(id).orElseThrow(
            () -> new MemberException("Invalid member")
        );
        return MemberInfoResponse.of(member);
    }
}
