package ecsimsw.picup.service;

import ecsimsw.picup.domain.Member;
import ecsimsw.picup.domain.MemberRepository;
import ecsimsw.picup.domain.Password;
import ecsimsw.picup.dto.MemberResponse;
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
//    private final StorageUsageService storageUsageService;

    @Transactional
    public MemberResponse signIn(SignInRequest request) {
        try {
            var member = getMember(request.username());
            member.authenticate(request.password());
//            var usage = storageUsageService.getUsage(member.getId());
//            return MemberResponse.of(member, usage);
            return MemberResponse.of(member);
        } catch (Exception e) {
            throw new MemberException("Invalid login info");
        }
    }

    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new MemberException("Duplicated username");
        }
        var password = Password.initFrom(request.password());
        var member = new Member(request.username(), password);
        memberRepository.save(member);
//        var usage = storageUsageService.init(member.getId());
//        return MemberResponse.of(member, usage);
        return MemberResponse.of(member);
    }

    @Transactional(readOnly = true)
    public MemberResponse me(String username) {
        var member = getMember(username);
//        var usage = storageUsageService.getUsage(member.getId());
        return MemberResponse.of(member);
//        return MemberResponse.of(member, usage);
    }

    private Member getMember(String username) {
        return memberRepository.findByUsername(username)
            .orElseThrow(() -> new MemberException("Invalid login info"));
    }
}
