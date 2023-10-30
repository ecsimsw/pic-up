package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.domain.StorageKey;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.utils.MockImageStorage;
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
import static ecsimsw.picup.utils.FileFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageServiceBackUpBackUpTest {

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

    @Nested
    class UploadTest {

        @Captor
        private ArgumentCaptor<Resource> resourceArgumentCaptor;

        @BeforeEach
        public void initRepository() {
            when(resourceRepository.save(any(Resource.class))).thenAnswer(i -> i.getArguments()[0]);
        }

        @DisplayName("업로드 성공")
        @Test
        public void uploadSuccessfully() {
            var result = storageService.upload(fakeTag, mockMultipartFile);
            verify(resourceRepository, times(3))
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

        @DisplayName("Main storage 에 저장 실패시 업로드에 실패한다. 단, 생성 기록은 남긴다.")
        @Test
        public void uploadFailWithMainStorage() {
            doThrow(StorageException.class)
                .when(mainStorage)
                .create(any(String.class), any(ImageFile.class));

            assertThrows(StorageException.class, () -> storageService.upload(fakeTag, mockMultipartFile));
            verify(resourceRepository, times(1))
                .save(resourceArgumentCaptor.capture());

            var savedResource = resourceArgumentCaptor.getValue();
            assertAll(
                () -> assertThat(savedResource.getResourceKey()).isNotNull(),
                () -> assertThat(savedResource.getCreateRequested()).isNotNull(),
                () -> assertThat(savedResource.getStoredStorages()).isEqualTo(Collections.emptyList())
            );
        }

        @DisplayName("BackUp storage 에 저장 실패시 업로드에 실패한다. 단, 생성 기록은 남긴다.")
        @Test
        public void uploadFailWithBackUpStorage() {
            doThrow(StorageException.class)
                .when(backUpStorage)
                .create(any(String.class), any(ImageFile.class));

            assertThrows(StorageException.class, () -> storageService.upload(fakeTag, mockMultipartFile));
            verify(resourceRepository, times(2))
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
            resourceKey = storageService.upload(fakeTag, mockMultipartFile).getResourceKey();
            when(resourceRepository.findById(resourceKey))
                .thenReturn(Optional.of(createdResource(resourceKey)));
        }

        @DisplayName("읽기 성공")
        @Test
        public void readSuccessfully() {
            var result = storageService.read(resourceKey);
            assertAll(
                () -> assertThat(result.getImageFile()).isNotNull(),
                () -> assertThat(result.getFileType()).isNotNull(),
                () -> assertThat(result.getMediaType()).isNotNull()
            );
        }

        @DisplayName("Main storage 에 파일이 없는 경우 BackUp storage에서 load 한다.")
        @Test
        public void readFromBackUpStorage() throws FileNotFoundException {
            doThrow(StorageException.class)
                .when(mainStorage)
                .read(any(String.class));

            var result = storageService.read(resourceKey);
            assertThat(result.getImageFile()).isNotNull();

            verify(mainStorage, times(1))
                .create(eq(resourceKey), any(ImageFile.class));

            verify(resourceRepository, times(3))
                .save(resourceArgumentCaptor.capture());

            var saved = resourceArgumentCaptor.getValue();
            assertThat(saved.getStoredStorages()).contains(LOCAL_FILE_STORAGE);
        }
    }
}
