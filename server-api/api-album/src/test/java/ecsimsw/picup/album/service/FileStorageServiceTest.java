//package ecsimsw.picup.album.service;
//
//import ecsimsw.picup.album.domain.FileDeletionEventOutbox;
//import ecsimsw.picup.album.domain.PictureFile;
//import ecsimsw.picup.album.exception.AlbumException;
//import ecsimsw.picup.album.exception.UnsupportedFileTypeException;
//import ecsimsw.picup.dto.ImageFileUploadRequest;
//import ecsimsw.picup.dto.ImageFileUploadResponse;
//import ecsimsw.picup.mq.ImageFileMessageQueue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static ecsimsw.picup.env.AlbumFixture.*;
//import static ecsimsw.picup.env.MemberFixture.MEMBER_ID;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.assertAll;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class FileStorageServiceTest {
//
//    @Mock
//    private StorageHttpClient storageHttpClient;
//
//    @Mock
//    private FileDeletionEventOutbox fileDeletionEventOutbox;
//
//    @Mock
//    private ImageFileMessageQueue imageFileMessageQueue;
//
//    private FileStorageService fileStorageService;
//
//    @BeforeEach
//    public void init() {
//        fileStorageService = new FileStorageService(storageHttpClient, fileDeletionEventOutbox, imageFileMessageQueue);
//    }
//
//    @DisplayName("파일을 업로드하고 업로드한 파일 정보를 반환한다.")
//    @Test
//    void upload() {
//        when(storageHttpClient.requestUploadImage(new ImageFileUploadRequest(MEMBER_ID, MULTIPART_FILE, RESOURCE_KEY)))
//            .thenReturn(new ImageFileUploadResponse(RESOURCE_KEY, MULTIPART_FILE.getSize()));
//
//        var fileInfo = fileStorageService.uploadImage(PictureFile.of(MULTIPART_FILE));
//        assertAll(
//            () -> assertThat(fileInfo.resourceKey()).isEqualTo(RESOURCE_KEY),
//            () -> assertThat(fileInfo.size()).isEqualTo(MULTIPART_FILE.getSize())
//        );
//    }
//
//    @DisplayName("파일 업로드시 올바르지 않은 파일 이름을 확인하고 예외를 발생시킨다.")
//    @Test
//    void uploadWithInvalidResourceName() {
//        var invalidFileName = "invalidFileName";
//        assertThatThrownBy(
//            () -> fileStorageService.uploadImage(PictureFile.of(mockMultipartFile(invalidFileName)))
//        ).isInstanceOf(AlbumException.class);
//    }
//
//    @DisplayName("지원하지 않는 파일 형식을 확인하고 예외를 발생시킨다.")
//    @Test
//    void uploadWithInvalidResource() {
//        var invalidFileExtension = "unsupportedFileExtension.mp4";
//        assertThatThrownBy(
//            () -> fileStorageService.uploadImage(PictureFile.of(mockMultipartFile(invalidFileExtension)))
//        ).isInstanceOf(UnsupportedFileTypeException.class);
//    }
//}
