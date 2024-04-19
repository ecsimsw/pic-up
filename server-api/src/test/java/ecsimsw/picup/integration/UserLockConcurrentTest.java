package ecsimsw.picup.integration;

import ecsimsw.picup.album.service.AlbumService;
import ecsimsw.picup.album.service.MemberService;
import ecsimsw.picup.album.service.PictureUploadService;
import ecsimsw.picup.album.service.StorageUsageService;
import ecsimsw.picup.config.S3MockConfig;
import io.findify.s3mock.S3Mock;
import javax.annotation.PreDestroy;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.annotation.AfterTestClass;

import static ecsimsw.picup.env.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.env.AlbumFixture.IMAGE_FILE;
import static ecsimsw.picup.env.MemberFixture.SIGN_UP_REQUEST;
import static ecsimsw.picup.utils.ConcurrentJobTestUtils.concurrentJob;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("사용자 락으로 동시 업로드시 공유 자원을 격리한다.")
@ActiveProfiles("test")
@Import(S3MockConfig.class)
@SpringBootTest
public class UserLockConcurrentTest {

    @Autowired
    private PictureUploadService pictureUploadService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private StorageUsageService storageUsageService;

    private Long userId;
    private Long albumId;

    @BeforeEach
    public void init() {
        userId = memberService.signUp(SIGN_UP_REQUEST).getId();
        albumId = albumService.create(userId, ALBUM_NAME, IMAGE_FILE);
    }

    @DisplayName("이미지 동시 업로드, 스토리지 사용량 정상 업데이트를 확인한다.")
    @Test
    public void uploadPictures() {
        int number = 100;
        var uploadFile = IMAGE_FILE;
        concurrentJob(
            number,
            () -> pictureUploadService.createImagePicture(userId, albumId, uploadFile, uploadFile)
        );
        assertThat(uploadFile.size() * number)
            .isEqualTo(storageUsageService.getUsage(userId).getUsageAsByte());
    }
}
