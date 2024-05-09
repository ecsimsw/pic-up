package ecsimsw.picup.integration;

import static ecsimsw.picup.utils.MemberFixture.USER_NAME;
import static ecsimsw.picup.utils.MemberFixture.USER_PASSWORD;

import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.storage.domain.FileResourceRepository;
import ecsimsw.picup.album.domain.MemberRepository;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {RedisConfig.class})
public class IntegrationTestContext {

    protected static long savedUserId;

    @BeforeAll
    static void initMember(@Autowired MemberService memberService) {
        savedUserId = memberService.signUp(new SignUpRequest(USER_NAME, USER_PASSWORD)).id();
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
    }
}
