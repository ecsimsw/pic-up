package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.FileDeletionEventOutbox;
import ecsimsw.picup.album.domain.ImageFile;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.exception.UnsupportedFileTypeException;
import ecsimsw.picup.dto.FileUploadRequest;
import ecsimsw.picup.dto.FileUploadResponse;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private StorageHttpClient storageHttpClient;

    @Mock
    private FileDeletionEventOutbox fileDeletionEventOutbox;

    @Mock
    private ImageFileMessageQueue imageFileMessageQueue;

    private FileStorageService fileStorageService;

    @BeforeEach
    public void init() {
        fileStorageService = new FileStorageService(storageHttpClient, fileDeletionEventOutbox, imageFileMessageQueue);
    }

    @DisplayName("파일을 업로드하고 업로드한 파일 정보를 반환한다.")
    @Test
    void upload() {
        when(storageHttpClient.requestUpload(new FileUploadRequest(MEMBER_ID, MULTIPART_FILE, RESOURCE_KEY)))
            .thenReturn(new FileUploadResponse(RESOURCE_KEY, MULTIPART_FILE.getSize()));

        var fileInfo = fileStorageService.upload(ImageFile.of(MULTIPART_FILE));
        assertAll(
            () -> assertThat(fileInfo.resourceKey()).isEqualTo(RESOURCE_KEY),
            () -> assertThat(fileInfo.size()).isEqualTo(MULTIPART_FILE.getSize())
        );
    }

    @DisplayName("파일 업로드시 올바르지 않은 파일 이름을 확인하고 예외를 발생시킨다.")
    @Test
    void uploadWithInvalidResourceName() {
        var invalidFileName = "invalidFileName";
        assertThatThrownBy(
            () -> fileStorageService.upload(ImageFile.of(mockMultipartFile(invalidFileName)))
        ).isInstanceOf(AlbumException.class);
    }

    @DisplayName("지원하지 않는 파일 형식을 확인하고 예외를 발생시킨다.")
    @Test
    void uploadWithInvalidResource() {
        var invalidFileExtension = "unsupportedFileExtension.mp4";
        assertThatThrownBy(
            () -> fileStorageService.upload(ImageFile.of(mockMultipartFile(invalidFileExtension)))
        ).isInstanceOf(UnsupportedFileTypeException.class);
    }
}
