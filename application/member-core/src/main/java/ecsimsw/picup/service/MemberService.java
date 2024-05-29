package ecsimsw.picup.service;

import ecsimsw.picup.domain.Member;
import ecsimsw.picup.domain.SignUpEvent;
import ecsimsw.picup.domain.SignUpEventRepository;
import ecsimsw.picup.domain.MemberRepository;
import ecsimsw.picup.dto.MemberInfo;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
import ecsimsw.picup.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final SignUpEventRepository signUpEventRepository;

    @Transactional
    public MemberInfo signIn(SignInRequest request) {
        try {
            var member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new MemberException("Invalid login info"));
            member.authenticate(request.password());
            return MemberInfo.of(member);
        } catch (Exception e) {
            throw new MemberException("Invalid login info");
        }
    }

    @Transactional
    public MemberInfo signUp(SignUpRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new MemberException("Duplicated username");
        }
        var member = Member.signUp(request.username(), request.password());
        memberRepository.save(member);

        signUpEventRepository.save(SignUpEvent.from(member, Long.MAX_VALUE));
        return MemberInfo.of(member);
    }

    @Transactional
    public void delete(Long userId) {
        memberRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public MemberInfo me(long userId) {
        var member = memberRepository.findById(userId)
            .orElseThrow(() -> new MemberException("Invalid login info"));
        return MemberInfo.of(member);
    }
}