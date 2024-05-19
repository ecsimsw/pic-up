package ecsimsw.picup.integration;

import static ecsimsw.picup.utils.MemberFixture.USER_NAME;
import static ecsimsw.picup.utils.MemberFixture.USER_PASSWORD;

import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.storage.domain.FileResourceRepository;
import ecsimsw.picup.domain.MemberRepository;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.dto.SignUpRequest;
import ecsimsw.picup.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {RedisConfig.class})
public class ServiceIntegrationTestContext {

    protected static long savedUserId;

    @BeforeAll
    static void initMember(@Autowired MemberService memberService) {
        savedUserId = memberService.signUp(new SignUpRequest(USER_NAME, USER_PASSWORD)).id();

        // A 데이터를 만들고
    }

    @AfterEach
    void clearAll(
        @Autowired MemberRepository memberRepository,
        @Autowired PictureRepository pictureRepository,
        @Autowired AlbumRepository albumRepository,
        @Autowired FileResourceRepository fileResourceRepository
    ) {
        fileResourceRepository.deleteAll();
        pictureRepository.deleteAll();
        albumRepository.deleteAll();
        memberRepository.deleteAll();

        // A 데이터를 지운다.
    }
}
