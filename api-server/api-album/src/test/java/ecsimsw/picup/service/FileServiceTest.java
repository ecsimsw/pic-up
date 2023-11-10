package ecsimsw.picup.service;

import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.dto.ImageFileInfo;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.exception.MessageQueueServerDownException;
import ecsimsw.picup.exception.UnsupportedFileTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.MEMBER_ID;
import static ecsimsw.picup.service.FileService.FILE_DELETION_SEGMENT_UNIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private StorageHttpClient storageHttpClient;

    @Mock
    private StorageMessageQueue storageMessageQueue;

    @Mock
    private FileResourceRepository fileResourceRepository;

    private FileService fileService;

    @BeforeEach
    private void init() {
        fileService = new FileService(fileResourceRepository, storageHttpClient, storageMessageQueue);
    }

    @DisplayName("파일 다중 삭제는 지정된 개수로 나눠 처리된다.")
    @Test
    void deleteAll() throws MessageQueueServerDownException {
        var resources = List.of(RESOURCE_KEY, RESOURCE_KEY, RESOURCE_KEY, RESOURCE_KEY, RESOURCE_KEY, RESOURCE_KEY);
        fileService.deleteAll(resources);

        var expectedSegmentCount = resources.size() / FILE_DELETION_SEGMENT_UNIT;
        verify(storageMessageQueue, times(expectedSegmentCount))
            .pollDeleteRequest(List.of(RESOURCE_KEY));
    }

    @DisplayName("파일을 업로드하고 업로드한 파일 정보를 반환한다.")
    @Test
    void upload() {
        when(storageHttpClient.requestUpload(MEMBER_ID, MULTIPART_FILE, TAG))
            .thenReturn(new ImageFileInfo(RESOURCE_KEY, MULTIPART_FILE.getSize()));

        var fileInfo = fileService.upload(MEMBER_ID, MULTIPART_FILE, TAG);
        assertAll(
            () -> assertThat(fileInfo.getResourceKey()).isEqualTo(RESOURCE_KEY),
            () -> assertThat(fileInfo.getSize()).isEqualTo(MULTIPART_FILE.getSize())
        );
    }

    @DisplayName("파일을 삭제 처리한다.")
    @Test
    void delete() throws MessageQueueServerDownException {
        fileService.delete(RESOURCE_KEY);

        verify(storageMessageQueue, atLeastOnce())
            .pollDeleteRequest(List.of(RESOURCE_KEY));
    }

    @DisplayName("MessageQueue 의 오류로 파일 삭제에 실패한 리소스는 Garbage file 이라는 이름으로 영속된다.")
    @Test
    void failedAfterRetry() throws MessageQueueServerDownException {
        doThrow(MessageQueueServerDownException.class)
            .when(storageMessageQueue).pollDeleteRequest(List.of(RESOURCE_KEY));

        assertDoesNotThrow(
            () -> fileService.delete(RESOURCE_KEY)
        );

        verify(storageMessageQueue, atLeastOnce())
            .pollDeleteRequest(List.of(RESOURCE_KEY));

        verify(fileResourceRepository, atLeastOnce())
            .saveAll(any());
    }

    @DisplayName("파일 업로드시 올바르지 않은 파일 이름을 확인하고 예외를 발생시킨다.")
    @Test
    void uploadWithInvalidResourceName() {
        var invalidFileName = "invalidFileName";
        assertThatThrownBy(
            () -> fileService.upload(MEMBER_ID, mockMultipartFile(invalidFileName), TAG)
        ).isInstanceOf(AlbumException.class);
    }

    @DisplayName("지원하지 않는 파일 형식을 확인하고 예외를 발생시킨다.")
    @Test
    void uploadWithInvalidResource() {
        var invalidFileExtension = "unsupportedFileExtension.mp4";
        assertThatThrownBy(
            () -> fileService.upload(MEMBER_ID, mockMultipartFile(invalidFileExtension), TAG)
        ).isInstanceOf(UnsupportedFileTypeException.class);
    }
}
