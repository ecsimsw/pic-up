package ecsimsw.picup.config.dev;

import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Profile("dev")
@Component
public class MockDataService {

    private final MemberService memberService;

    @PostConstruct
    public void init() {
        memberService.signUp(
            new SignUpRequest("ecsimsw", "publicUserForTest")
        );
    }
}
