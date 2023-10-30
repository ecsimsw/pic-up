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

import java.util.Collections;
import java.util.List;

import static ecsimsw.picup.utils.FileFixture.mockFile;
import static ecsimsw.picup.utils.FileFixture.mockTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Spy
    private ImageStorage mainStorage = new MockImageStorage();

    @Spy
    private ImageStorage backUpStorage = new MockImageStorage();

    private ResourceService resourceService;

    @BeforeEach
    public void init() {
        resourceService = new ResourceService(resourceRepository, mainStorage, backUpStorage);
    }

    @Captor
    private ArgumentCaptor<Resource> resourceArgumentCaptor;

    @Nested
    class UploadTest {

        @BeforeEach
        public void initRepository() {
            when(resourceRepository.save(any(Resource.class))).thenAnswer(i -> i.getArguments()[0]);
        }

        @DisplayName("업로드 성공")
        @Test
        public void uploadSuccessfully() {
            var result = resourceService.upload(mockTag, mockFile);
            verify(resourceRepository, times(3))
                .save(resourceArgumentCaptor.capture());

            var savedResource = resourceArgumentCaptor.getValue();
            assertAll(
                () -> assertNotNull(result.getResourceKey()),
                () -> assertThat(savedResource.getResourceKey()).isEqualTo(result.getResourceKey()),
                () -> assertThat(savedResource.getStoredStorages()).isEqualTo(
                    List.of(StorageKey.MAIN_STORAGE, StorageKey.BACKUP_STORAGE)
                )
            );
        }

        @DisplayName("Main storage 에 저장 실패시 업로드에 실패한다. 단, 생성 기록은 남긴다.")
        @Test
        public void uploadFailWithMainStorage() {
            doThrow(StorageException.class)
                .when(mainStorage)
                .create(any(String.class), any(ImageFile.class));

            assertThrows(StorageException.class, () -> resourceService.upload(mockTag, mockFile));
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

            assertThrows(StorageException.class, () -> resourceService.upload(mockTag, mockFile));
            verify(resourceRepository, times(2))
                .save(resourceArgumentCaptor.capture());

            var savedResource = resourceArgumentCaptor.getValue();
            assertAll(
                () -> assertThat(savedResource.getResourceKey()).isNotNull(),
                () -> assertThat(savedResource.getCreateRequested()).isNotNull(),
                () -> assertThat(savedResource.getStoredStorages()).isEqualTo(List.of(StorageKey.MAIN_STORAGE))
            );
        }
    }
}
