package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.domain.StorageKey;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.env.MockImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static ecsimsw.picup.domain.StorageKey.LOCAL_FILE_STORAGE;
import static ecsimsw.picup.domain.StorageKey.S3_OBJECT_STORAGE;
import static ecsimsw.picup.env.FileFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Spy
    private ImageStorage mainStorage = new MockImageStorage(LOCAL_FILE_STORAGE);

    @Spy
    private ImageStorage backUpStorage = new MockImageStorage(S3_OBJECT_STORAGE);

    private StorageService storageService;

    @BeforeEach
    public void init() {
        storageService = new StorageService(resourceRepository, mainStorage, backUpStorage);
    }

    @DisplayName("Upload")
    @Nested
    class UploadTest {

        @Captor
        private ArgumentCaptor<Resource> resourceArgumentCaptor;

        @BeforeEach
        public void initRepository() {
            when(resourceRepository.save(any(Resource.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        }

        @DisplayName("업로드 성공")
        @Test
        public void uploadSuccessfully() {
            var result = storageService.upload(FILE_TAG, MULTIPART_FILE);
            verify(resourceRepository, atLeast(3))
                .save(resourceArgumentCaptor.capture());

            var savedResource = resourceArgumentCaptor.getValue();
            assertAll(
                () -> assertNotNull(result.getResourceKey()),
                () -> assertThat(savedResource.getResourceKey()).isEqualTo(result.getResourceKey()),
                () -> assertThat(savedResource.getStoredStorages()).isEqualTo(
                    List.of(LOCAL_FILE_STORAGE, StorageKey.S3_OBJECT_STORAGE)
                )
            );
        }

        @DisplayName("Main storage 에 저장 실패시 업로드에 실패한다. 단, 리소스 생성 기록은 남긴다.")
        @Test
        public void uploadFailWithMainStorage() {
            doThrow(StorageException.class)
                .when(mainStorage).create(any(String.class), any(ImageFile.class));

            assertThrows(StorageException.class, () -> storageService.upload(FILE_TAG, MULTIPART_FILE));
            verify(resourceRepository, atLeast(1))
                .save(resourceArgumentCaptor.capture());

            var savedResource = resourceArgumentCaptor.getValue();
            assertAll(
                () -> assertThat(savedResource.getResourceKey()).isNotNull(),
                () -> assertThat(savedResource.getCreateRequested()).isNotNull(),
                () -> assertThat(savedResource.getStoredStorages()).isEqualTo(Collections.emptyList())
            );
        }

        @DisplayName("BackUp storage 에 저장 실패시 업로드에 실패한다. 단, Main 스토리지까지의 저장 기록은 남긴다.")
        @Test
        public void uploadFailWithBackUpStorage() {
            doThrow(StorageException.class)
                .when(backUpStorage).create(any(String.class), any(ImageFile.class));

            assertThrows(StorageException.class, () -> storageService.upload(FILE_TAG, MULTIPART_FILE));
            verify(resourceRepository, atLeast(2))
                .save(resourceArgumentCaptor.capture());

            var savedResource = resourceArgumentCaptor.getValue();
            assertAll(
                () -> assertThat(savedResource.getResourceKey()).isNotNull(),
                () -> assertThat(savedResource.getCreateRequested()).isNotNull(),
                () -> assertThat(savedResource.getStoredStorages()).isEqualTo(List.of(LOCAL_FILE_STORAGE))
            );
        }
    }

    @Nested
    class ReadTest {

        @Captor
        private ArgumentCaptor<Resource> resourceArgumentCaptor;

        private String resourceKey;

        @BeforeEach
        private void init() {
            resourceKey = storageService.upload(FILE_TAG, MULTIPART_FILE).getResourceKey();
            when(resourceRepository.findById(resourceKey))
                .thenReturn(Optional.of(createdResource(resourceKey)));
        }

        @DisplayName("Read")
        @Test
        public void readSuccessfully() {
            var result = storageService.read(resourceKey);
            assertAll(
                () -> assertThat(result.getImageFile()).isNotNull(),
                () -> assertThat(result.getFileType()).isNotNull(),
                () -> assertThat(result.getMediaType()).isNotNull()
            );
        }

        @DisplayName("Main storage 에 파일이 없는 경우 BackUp storage 에서 Main 으로 load 하고 응답한다.")
        @Test
        public void readFailed1() throws FileNotFoundException {
            doThrow(FileNotFoundException.class)
                .when(mainStorage).read(any(String.class));

            var result = storageService.read(resourceKey);
            assertThat(result.getImageFile()).isNotNull();

            verify(mainStorage, atLeast(2))
                .create(eq(resourceKey), any(ImageFile.class));

            verify(resourceRepository, atLeast(5))
                .save(resourceArgumentCaptor.capture());

            var saved = resourceArgumentCaptor.getValue();
            assertThat(saved.getStoredStorages()).contains(LOCAL_FILE_STORAGE);
            assertThat(saved.getStoredStorages()).contains(S3_OBJECT_STORAGE);
        }

        @DisplayName("Main storage 에 파일이 없고, 쓰기에 실패하는 경우 저장 정보 변경 없이 BackUp 에서 직접 응답한다.")
        @Test
        public void readFailed2() throws FileNotFoundException {
            doThrow(FileNotFoundException.class)
                .when(mainStorage).read(any(String.class));

            doThrow(StorageException.class)
                .when(mainStorage).create(any(String.class), any(ImageFile.class));

            var result = storageService.read(resourceKey);
            assertThat(result.getImageFile()).isNotNull();

            verify(mainStorage, atLeast(2))
                .create(eq(resourceKey), any(ImageFile.class));

            verify(resourceRepository, atLeast(4))
                .save(resourceArgumentCaptor.capture());

            var saved = resourceArgumentCaptor.getValue();
            assertThat(saved.getStoredStorages()).doesNotContain(LOCAL_FILE_STORAGE);
            assertThat(saved.getStoredStorages()).contains(S3_OBJECT_STORAGE);
        }

        @DisplayName("Main storage 에서 읽기 실패할 경우 BackUp storage 에서 응답한다.")
        @Test
        public void readFailed3() throws FileNotFoundException {
            doThrow(StorageException.class)
                .when(mainStorage).read(any(String.class));

            var result = storageService.read(resourceKey);
            assertThat(result.getImageFile()).isNotNull();

            verify(mainStorage, times(1))
                .create(eq(resourceKey), any(ImageFile.class));

            verify(resourceRepository, atLeast(3))
                .save(resourceArgumentCaptor.capture());

            var saved = resourceArgumentCaptor.getValue();
            assertThat(saved.getStoredStorages()).contains(LOCAL_FILE_STORAGE);
            assertThat(saved.getStoredStorages()).contains(S3_OBJECT_STORAGE);
        }

        @DisplayName("모든 storage 에서 파일이 존재하지 않는 경우 읽기에 저장 정보를 변경하고 읽기에 실패한다.")
        @Test
        public void readFailed4() throws FileNotFoundException {
            doThrow(FileNotFoundException.class)
                .when(mainStorage).read(any(String.class));

            doThrow(FileNotFoundException.class)
                .when(backUpStorage).read(any(String.class));

            assertThatThrownBy(
                () -> storageService.read(resourceKey)
            ).isInstanceOf(StorageException.class);

            verify(resourceRepository, atLeast(5))
                .save(resourceArgumentCaptor.capture());

            var saved = resourceArgumentCaptor.getValue();
            assertThat(saved.getStoredStorages()).doesNotContain(LOCAL_FILE_STORAGE);
            assertThat(saved.getStoredStorages()).doesNotContain(S3_OBJECT_STORAGE);
        }

        @DisplayName("모든 storage 에서 읽기 실패하는 경우 저장 정보 변경 없이 읽기에 실패한다.")
        @Test
        public void readFailed5() throws FileNotFoundException {
            doThrow(StorageException.class)
                .when(mainStorage).read(any(String.class));

            doThrow(StorageException.class)
                .when(backUpStorage).read(any(String.class));

            assertThatThrownBy(
                () -> storageService.read(resourceKey)
            ).isInstanceOf(StorageException.class);

            verify(resourceRepository, atLeast(3))
                .save(resourceArgumentCaptor.capture());

            var saved = resourceArgumentCaptor.getValue();
            assertThat(saved.getStoredStorages()).contains(LOCAL_FILE_STORAGE);
            assertThat(saved.getStoredStorages()).contains(S3_OBJECT_STORAGE);
        }
    }

    @DisplayName("Delete")
    @Nested
    class DeleteTest {

        @Captor
        private ArgumentCaptor<Resource> resourceArgumentCaptor;

        private String resourceKey;

        @BeforeEach
        private void init() {
            resourceKey = storageService.upload(FILE_TAG, MULTIPART_FILE).getResourceKey();
            when(resourceRepository.findById(resourceKey))
                .thenReturn(Optional.of(createdResource(resourceKey)));
        }

        @DisplayName("모든 스토리지에서 파일을 삭제하고, DB에 리소스가 에 삭제됨을 표시한다.")
        @Test
        public void deleteSuccessFully() {
            storageService.delete(resourceKey);

            verify(resourceRepository, atLeast(3))
                .save(resourceArgumentCaptor.capture());

            var saved = resourceArgumentCaptor.getValue();
            assertThat(saved.getStoredStorages()).isEmpty();
            assertThat(saved.getDeleteRequested()).isNotNull();
        }

        @DisplayName("Main Storage 에 파일이 존재하지 않는다면 이를 삭제 처리하고 BackUp Storage 에서의 삭제를 이어 진행한다.")
        @Test
        public void fileNotFoundOnMain() throws FileNotFoundException {
            doThrow(FileNotFoundException.class)
                .when(mainStorage).delete(any(String.class));

            storageService.delete(resourceKey);

            verify(resourceRepository, atLeast(3))
                .save(resourceArgumentCaptor.capture());

            var saved = resourceArgumentCaptor.getValue();
            assertThat(saved.getStoredStorages()).doesNotContain(LOCAL_FILE_STORAGE, S3_OBJECT_STORAGE);
            assertThat(saved.getDeleteRequested()).isNotNull();
        }

        @DisplayName("Main Storage 에서 파일 삭제가 실패하여도 BackUp Storage 에서 삭제를 진행하고 이를 표시한다.")
        @Test
        public void deleteFailedOnMain() throws FileNotFoundException {
            doThrow(StorageException.class)
                .when(mainStorage).delete(any(String.class));

            storageService.delete(resourceKey);

            verify(resourceRepository, atLeast(3))
                .save(resourceArgumentCaptor.capture());

            var saved = resourceArgumentCaptor.getValue();
            assertThat(saved.getStoredStorages()).contains(LOCAL_FILE_STORAGE);
            assertThat(saved.getStoredStorages()).doesNotContain(S3_OBJECT_STORAGE);
            assertThat(saved.getDeleteRequested()).isNotNull();
        }

        @DisplayName("BackUp Storage 에 파일이 존재하지 않는다면 이를 삭제 처리한다.")
        @Test
        public void fileNotFoundOnBackUp() throws FileNotFoundException {
            doThrow(FileNotFoundException.class)
                .when(backUpStorage).delete(any(String.class));

            storageService.delete(resourceKey);

            verify(resourceRepository, atLeast(3))
                .save(resourceArgumentCaptor.capture());

            var saved = resourceArgumentCaptor.getValue();
            assertThat(saved.getStoredStorages()).doesNotContain(LOCAL_FILE_STORAGE, S3_OBJECT_STORAGE);
            assertThat(saved.getDeleteRequested()).isNotNull();
        }

        @DisplayName("Storage 에서 삭제 처리가 실패한다면 삭제 요청 시점을 표시하고 storage 저장 정보를 남긴다.")
        @Test
        public void deleteAllFailedFromStorages() throws FileNotFoundException {
            doThrow(StorageException.class)
                .when(mainStorage).delete(any(String.class));

            doThrow(StorageException.class)
                .when(backUpStorage).delete(any(String.class));

            storageService.delete(resourceKey);

            verify(resourceRepository, atLeast(3))
                .save(resourceArgumentCaptor.capture());

            var saved = resourceArgumentCaptor.getValue();
            assertThat(saved.getStoredStorages()).contains(LOCAL_FILE_STORAGE, S3_OBJECT_STORAGE);
            assertThat(saved.getDeleteRequested()).isNotNull();
        }
    }
}
