package ecsimsw.picup.integration;

import ecsimsw.picup.album.service.AlbumService;
import ecsimsw.picup.album.service.FileService;
import ecsimsw.picup.album.service.PictureUploadService;
import ecsimsw.picup.member.dto.SignUpRequest;
import ecsimsw.picup.member.service.MemberService;
import ecsimsw.picup.member.service.StorageUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static ecsimsw.picup.env.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.env.AlbumFixture.IMAGE_FILE;
import static org.assertj.core.api.Assertions.assertThat;


@TestPropertySource(locations = "/databaseConfig.properties")
@SpringBootTest
public class StorageUsageConcurrentTest {

    @Autowired
    private PictureUploadService pictureUploadService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private StorageUsageService storageUsageService;

    private Long memberId;
    private Long albumId;

    @BeforeEach
    public void init() {
        memberId = memberService.signUp(new SignUpRequest("USERNAME", "PASSWORD")).getId();
        albumId = albumService.create(memberId, ALBUM_NAME, IMAGE_FILE()).id();
    }

    @DisplayName("동시 업로드 동시성 문제를 테스트한다.")
    @Test
    public void uploadConcurrentRequest() throws InterruptedException {
        int CONCURRENT_COUNT = 100;
        var uploadFile = IMAGE_FILE();

        var executorService = Executors.newFixedThreadPool(CONCURRENT_COUNT);
        var countDownLatch = new CountDownLatch(CONCURRENT_COUNT);
        for (int i = 0; i < CONCURRENT_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    pictureUploadService.uploadImage(memberId, albumId, uploadFile, uploadFile);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        assertThat(uploadFile.size() * CONCURRENT_COUNT)
            .isEqualTo(storageUsageService.getUsage(memberId).getUsageAsByte());
    }
}
