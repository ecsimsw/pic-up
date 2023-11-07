package ecsimsw.picup.service;

import ecsimsw.picup.dto.ImageFileInfo;
import ecsimsw.picup.exception.AlbumException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ecsimsw.picup.env.AlbumFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private StorageHttpClient storageHttpClient;

    @Mock
    private StorageMessageQueue storageMessageQueue;

    private FileService fileService;

    @BeforeEach
    private void init() {
        fileService = new FileService(storageHttpClient, storageMessageQueue);
    }

    @DisplayName("파일 업로드시 storage server 에 업로드를 요청하고 생성된 리소스 정보를 반환받는다.")
    @Test
    void upload() {
        when(storageHttpClient.requestUpload(MULTIPART_FILE, TAG))
            .thenReturn(new ImageFileInfo(RESOURCE_KEY, SIZE));

        var fileInfo = fileService.upload(MULTIPART_FILE, TAG);
        assertAll(
            () -> assertThat(fileInfo.getResourceKey()).isEqualTo(RESOURCE_KEY),
            () -> assertThat(fileInfo.getSize()).isEqualTo(SIZE)
        );
    }

    @DisplayName("지원하지 않는 파일 타입시 storage server 에 요청하지 않고 예외를 발생시킨다.")
    @Test
    void uploadWithInvalidFileType() {
        assertThatThrownBy(
            () -> fileService.upload(INVALID_MULTIPART_FILE, TAG)
        ).isInstanceOf(AlbumException.class);
        verify(storageHttpClient, never()).requestUpload(INVALID_MULTIPART_FILE, TAG);
    }

    @Test
    void delete() {
    }

    @Test
    void deleteAll() {
    }
}