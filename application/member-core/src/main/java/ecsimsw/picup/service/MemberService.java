package ecsimsw.picup.service;

import ecsimsw.picup.domain.Member;
import ecsimsw.picup.domain.MemberEvent;
import ecsimsw.picup.domain.MemberEventRepository;
import ecsimsw.picup.domain.MemberRepository;
import ecsimsw.picup.dto.MemberInfo;
import ecsimsw.picup.exception.MemberException;
import ecsimsw.picup.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberEventRepository memberEventRepository;

    @Transactional
    public MemberInfo signIn(String username, String password) {
        try {
            var member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException("Invalid login info"));
            member.authenticate(password);
            return MemberInfo.of(member);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid login info");
        }
    }

    @Transactional
    public MemberInfo signUp(String username, String password) {
        if (memberRepository.existsByUsername(username)) {
            throw new MemberException("Duplicated username");
        }
        var member = Member.signUp(username, password);
        memberRepository.save(member);
        memberEventRepository.save(MemberEvent.created(member.getId(), Long.MAX_VALUE));
        return MemberInfo.of(member);
    }

    @Transactional
    public void delete(Long userId) {
        if (!memberRepository.existsById(userId)) {
            throw new MemberException("Not exists member");
        }
        memberEventRepository.save(MemberEvent.deleted(userId));
        memberRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public MemberInfo me(long userId) {
        var member = memberRepository.findById(userId)
            .orElseThrow(() -> new MemberException("Invalid login info"));
        return MemberInfo.of(member);
    }
}
