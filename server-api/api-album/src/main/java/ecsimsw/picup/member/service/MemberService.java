package ecsimsw.picup.member.service;

import ecsimsw.picup.ecrypt.SHA256Utils;
import ecsimsw.picup.member.domain.Member;
import ecsimsw.picup.member.domain.MemberRepository;
import ecsimsw.picup.member.domain.Password;
import ecsimsw.picup.member.dto.MemberInfoResponse;
import ecsimsw.picup.member.dto.SignInRequest;
import ecsimsw.picup.member.dto.SignUpRequest;
import ecsimsw.picup.member.exception.LoginFailedException;
import ecsimsw.picup.member.exception.MemberException;
import ecsimsw.picup.usage.domain.StorageUsage;
import ecsimsw.picup.usage.domain.StorageUsageRepository;
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
            var member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new LoginFailedException("Invalid login info"));
            var requestPassword = encryptPassword(request.getPassword(), member.getPassword().getSalt());
            member.authenticate(requestPassword);
            return MemberInfoResponse.of(member);
        } catch (Exception e) {
            throw new LoginFailedException("Invalid login info");
        }
    }

    @Transactional
    public MemberInfoResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new MemberException("Duplicated username");
        }
        var password = encryptPassword(request.getPassword());
        var member = new Member(request.getUsername(), password);
        memberRepository.save(member);
        storageUsageRepository.save(new StorageUsage(member.getId(), 10000000000L));
        return MemberInfoResponse.of(member);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse me(Long id) {
        var member = memberRepository.findById(id).orElseThrow(
            () -> new MemberException("Invalid member")
        );
        return MemberInfoResponse.of(member);
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
}
