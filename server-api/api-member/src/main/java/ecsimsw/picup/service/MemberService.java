package ecsimsw.picup.service;

import ecsimsw.picup.domain.Member;
import ecsimsw.picup.domain.MemberRepository;
import ecsimsw.picup.domain.Password;
import ecsimsw.picup.dto.MemberInfoResponse;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
import ecsimsw.picup.ecrypt.EncryptService;
import ecsimsw.picup.ecrypt.SHA256EncryptResponse;
import ecsimsw.picup.exception.LoginFailedException;
import ecsimsw.picup.exception.MemberException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final EncryptService encryptService;

    public MemberService(MemberRepository memberRepository, EncryptService encryptService) {
        this.memberRepository = memberRepository;
        this.encryptService = encryptService;
    }

    @Transactional
    public MemberInfoResponse signIn(SignInRequest request) {
        try {
            final Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new LoginFailedException("Invalid login info"));
            final String salt = member.getPassword().getSalt();
            final Password requestPassword = encryptPassword(request.getPassword(), salt);
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
        final Password password = encryptPassword(request.getPassword());
        final Member member = new Member(request.getUsername(), password);
        memberRepository.save(member);
        return MemberInfoResponse.of(member);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse me(Long id) {
        final Member member = memberRepository.findById(id).orElseThrow(
            () -> new MemberException("Invalid member")
        );
        return MemberInfoResponse.of(member);
    }

    @NotNull
    private Password encryptPassword(String plainPassword) {
        final String salt = encryptService.issueSalt();
        return new Password(encryptService.encryptWithSHA256(plainPassword, salt), salt);
    }

    @NotNull
    private Password encryptPassword(String plainPassword, String salt) {
        return new Password(encryptService.encryptWithSHA256(plainPassword, salt), salt);
    }
}
