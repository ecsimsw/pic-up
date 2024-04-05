package ecsimsw.picup.member.service;

import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.ecrypt.SHA256Utils;
import ecsimsw.picup.member.domain.*;
import ecsimsw.picup.member.dto.MemberInfoResponse;
import ecsimsw.picup.member.dto.SignInRequest;
import ecsimsw.picup.member.dto.SignUpRequest;
import ecsimsw.picup.member.exception.LoginFailedException;
import ecsimsw.picup.member.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final StorageUsageRepository storageUsageRepository;

    @Transactional
    public MemberInfoResponse signIn(SignInRequest request) {
        try {
            var member = getMember(request.username());
            var requestPassword = encryptPassword(request.password(), member.getPassword().getSalt());
//            member.authenticate(requestPassword);
            var usage = getUsageByMember(member);
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
        var usage = storageUsageRepository.save(StorageUsage.init(member));
        return MemberInfoResponse.of(member, usage);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse me(String username) {
        var member = getMember(username);
        var usage = getUsageByMember(member);
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
            .orElseThrow(() -> new LoginFailedException("Invalid login info"));
    }

    private StorageUsage getUsageByMember(Member member) {
        return storageUsageRepository.findByUserId(member.getId()).orElseThrow(() -> new MemberException("Not exists member"));
    }
}
