package ecsimsw.picup.integration;

import static ecsimsw.picup.env.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.env.AlbumFixture.FILE_SIZE;
import static ecsimsw.picup.env.AlbumFixture.MULTIPART_FILE;
import static ecsimsw.picup.env.AlbumFixture.RESOURCE_KEY;
import static ecsimsw.picup.env.MemberFixture.USER_PASSWORD;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.exception.StorageException;
import ecsimsw.picup.album.service.AlbumCoreService;
import ecsimsw.picup.album.service.FileService;
import ecsimsw.picup.album.service.MemberService;
import ecsimsw.picup.album.service.PictureService;
import ecsimsw.picup.album.service.ThumbnailService;
import ecsimsw.picup.config.RedisConfig;
import ecsimsw.picup.config.S3MockConfig;
import ecsimsw.picup.album.dto.FileUploadResponse;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;

/*
Test async method
1. Inject custom ThreadPoolTaskExecutor -> awaitTermination
2. Inject custom ThreadPoolTaskExecutor -> countDownLatch
3. Inject SyncTaskExecutor -> async to sync
4. Mocking
 */

@DisplayName("파일 업로드 비동기 예외처리를 테스트한다.")
@SpringBootTest(classes = {S3MockConfig.class, RedisConfig.class})
class PictureUploadAsyncTest {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AlbumCoreService albumCoreService;

    @MockBean
    private FileService fileService;

    @MockBean
    private ThumbnailService thumbnailService;

    private long userId;
    private long albumId;

    @BeforeEach
    void skipThumbnailMaking() {
        userId = memberService.signUp(new SignUpRequest(UUID.randomUUID() + "", USER_PASSWORD)).getId();
        albumId = albumCoreService.create(userId, ALBUM_NAME, new FileUploadResponse(RESOURCE_KEY, FILE_SIZE));

        when(thumbnailService.resizeImage(any(MultipartFile.class), any(Float.class)))
            .thenAnswer(input -> input.getArgument(0));
    }

    @DisplayName("썸네일과 원본 이미지가 비동기 업로드된다.")
    @Test
    void uploadImage() {
        when(fileService.uploadFileAsync(any()))
            .thenReturn(completedFuture(new FileUploadResponse(RESOURCE_KEY, FILE_SIZE)));
        when(fileService.uploadImageThumbnailAsync(any(), any(Float.class)))
            .thenReturn(completedFuture(new FileUploadResponse(RESOURCE_KEY, FILE_SIZE)));

        pictureService.upload(userId, albumId, MULTIPART_FILE);
    }

    @DisplayName("업로드 중 예외가 발생할 경우, 정상 테스크는 업로드 완료 후 제거 로직이 실행된다.")
    @Test
    void uploadImageOneOfTasksFailed() {
        var uploaded = new FileUploadResponse(RESOURCE_KEY, FILE_SIZE);

        when(fileService.uploadFileAsync(any()))
            .thenReturn(failedFuture(new StorageException("Fail to store file")));
        when(fileService.uploadImageThumbnailAsync(any(), any(Float.class)))
            .thenReturn(completedFuture(uploaded));

        assertThatThrownBy(
            () -> pictureService.upload(userId, albumId, MULTIPART_FILE)
        ).isInstanceOf(AlbumException.class);

        InOrder orderVerifier = inOrder(fileService);
        orderVerifier.verify(fileService, times(1))
            .deleteAsync(uploaded.resourceKey());
    }

    @DisplayName("모든 업로드에서 예외가 발생할 경우, 제거 로직은 실행되지 않는다.")
    @Test
    void uploadImageAllOfTasksFailed() {
        when(fileService.uploadFileAsync(any()))
            .thenReturn(failedFuture(new StorageException("Fail to store file")));
        when(fileService.uploadImageThumbnailAsync(any(), any(Float.class)))
            .thenReturn(failedFuture(new StorageException("Fail to store file")));

        assertThatThrownBy(
            () -> pictureService.upload(userId, albumId, MULTIPART_FILE)
        ).isInstanceOf(AlbumException.class);

        InOrder orderVerifier = inOrder(fileService);
        orderVerifier.verify(fileService, times(0)).deleteAsync(RESOURCE_KEY);
    }
}