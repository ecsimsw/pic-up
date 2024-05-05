package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Member;
import ecsimsw.picup.album.domain.MemberRepository;
import ecsimsw.picup.album.domain.Password;
import ecsimsw.picup.album.dto.MemberResponse;
import ecsimsw.picup.album.dto.SignInRequest;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.exception.MemberException;
import ecsimsw.picup.auth.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private static final Long DEFAULT_STORAGE_LIMIT_BYTE = Long.MAX_VALUE;

    private final MemberRepository memberRepository;
    private final StorageUsageService storageUsageService;

    @Transactional
    public MemberResponse signIn(SignInRequest request) {
        try {
            var member = getMember(request.username());
            member.authenticate(request.password());
            var usage = storageUsageService.getUsage(member.getId());
            return MemberResponse.of(member, usage);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid login info");
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
        var usage = storageUsageService.init(member.getId(), DEFAULT_STORAGE_LIMIT_BYTE);
        return MemberResponse.of(member, usage);
    }

    @Transactional(readOnly = true)
    public MemberResponse me(String username) {
        var member = getMember(username);
        var usage = storageUsageService.getUsage(member.getId());
        return MemberResponse.of(member, usage);
    }

    private Member getMember(String username) {
        return memberRepository.findByUsername(username)
            .orElseThrow(() -> new MemberException("Invalid login info"));
    }
}
