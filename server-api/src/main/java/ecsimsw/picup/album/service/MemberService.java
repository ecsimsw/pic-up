package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Member;
import ecsimsw.picup.album.domain.MemberRepository;
import ecsimsw.picup.album.domain.Password;
import ecsimsw.picup.album.dto.MemberInfoResponse;
import ecsimsw.picup.album.dto.SignInRequest;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.exception.MemberException;
import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.ecrypt.SHA256Utils;
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
    public MemberInfoResponse signIn(SignInRequest request) {
        try {
            var member = getMember(request.username());
            var requestPassword = encryptPassword(request.password(), member.getPassword().getSalt());
//            member.authenticate(requestPassword);
            var usage = storageUsageService.getUsage(member.getId());
            return MemberInfoResponse.of(member, usage);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid login info");
        }
    }

    @Transactional
    public MemberInfoResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new MemberException("Duplicated username");
        }
        var password = encryptPassword(request.password());
        var member = new Member(request.username(), password);
        memberRepository.save(member);
        var usage = storageUsageService.init(member.getId(), DEFAULT_STORAGE_LIMIT_BYTE);
        return MemberInfoResponse.of(member, usage);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse me(String username) {
        var member = getMember(username);
        var usage = storageUsageService.getUsage(member.getId());
        return MemberInfoResponse.of(member, usage);
    }

    private Password encryptPassword(String plainPassword) {
        var salt = SHA256Utils.getSalt();
        return new Password(
            SHA256Utils.encrypt(plainPassword, salt),
            SHA256Utils.getSalt()
        );
    }

    private Password encryptPassword(String plainPassword, String salt) {
        return new Password(
            SHA256Utils.encrypt(plainPassword, salt),
            salt
        );
    }

    private Member getMember(String username) {
        return memberRepository.findByUsername(username)
            .orElseThrow(() -> new MemberException("Invalid login info"));
    }
}
