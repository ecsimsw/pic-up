package ecsimsw.picup.integration;

import static ecsimsw.picup.env.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.env.AlbumFixture.ORIGIN_FILE;
import static ecsimsw.picup.env.MemberFixture.SIGN_UP_REQUEST;
import static ecsimsw.picup.utils.ConcurrentJobTestUtils.concurrentJob;
import static org.assertj.core.api.Assertions.assertThat;

import ecsimsw.picup.album.service.*;
import ecsimsw.picup.album.service.FileResourceService;
import ecsimsw.picup.config.RedisConfig;
import ecsimsw.picup.config.S3MockConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@DisplayName("사용자 락으로 동시 업로드시 공유 자원을 격리한다.")
@SpringBootTest(classes = {S3MockConfig.class, RedisConfig.class})
public class UserLockConcurrentTest {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private StorageUsageService storageUsageService;

    @MockBean
    private FileResourceService fileService;

    @MockBean
    private ThumbnailService thumbnailService;

    private Long userId;
    private Long albumId;

    @BeforeEach
    public void init() {
        userId = memberService.signUp(SIGN_UP_REQUEST).id();
        albumId = albumService.create(userId, ALBUM_NAME, ORIGIN_FILE);
    }

    @DisplayName("이미지 동시 업로드, 스토리지 사용량 정상 업데이트를 확인한다.")
    @Test
    public void uploadPictures() {
        int number = 100;
        var uploadFile = ORIGIN_FILE;
        concurrentJob(
            number,
            () -> pictureService.createPicture(userId, albumId, uploadFile, uploadFile)
        );
        assertThat(uploadFile.size() * number)
            .isEqualTo(storageUsageService.getUsage(userId).getUsageAsByte());
    }
}
