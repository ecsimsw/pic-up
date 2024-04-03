package ecsimsw.picup.config;

import ecsimsw.picup.member.dto.SignUpRequest;
import ecsimsw.picup.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Component
public class PublicTesPageConfig {

    public static Long publicMemberId;

    private final MemberService service;

    @PostConstruct
    public void init() {
        var member = service.signUp(new SignUpRequest("공개 계정", "thisIsPublicUserForTest"));
        publicMemberId = member.getId();
    }
}
