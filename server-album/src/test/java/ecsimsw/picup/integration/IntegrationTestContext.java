package ecsimsw.picup.integration;

import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.FileResourceRepository;
import ecsimsw.picup.album.domain.MemberRepository;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.AlbumFacadeService;
import ecsimsw.picup.album.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static ecsimsw.picup.utils.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.utils.AlbumFixture.THUMBNAIL_FILE;
import static ecsimsw.picup.utils.MemberFixture.USER_NAME;
import static ecsimsw.picup.utils.MemberFixture.USER_PASSWORD;

@SpringBootTest(classes = {
    RedisConfig.class,
    S3MockConfig.class
})
public class IntegrationTestContext {

    protected long savedUserId;
    protected long savedAlbumId;

    @BeforeEach
    void init(
        @Autowired MemberService memberService,
        @Autowired AlbumFacadeService albumFacadeService
    ) {
        savedUserId = memberService.signUp(new SignUpRequest(USER_NAME, USER_PASSWORD)).id();
        savedAlbumId = albumFacadeService.init(savedUserId, ALBUM_NAME, THUMBNAIL_FILE);
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
