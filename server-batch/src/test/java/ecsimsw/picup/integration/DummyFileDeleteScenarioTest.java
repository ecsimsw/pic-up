package ecsimsw.picup.integration;

import static ecsimsw.picup.application.FileDeletionService.FILE_DELETION_RETRY_COUNTS;
import static ecsimsw.picup.storage.domain.StorageType.STORAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ecsimsw.picup.application.FileDeletionService;
import ecsimsw.picup.domain.FileDeletionFailedLogRepository;
import ecsimsw.picup.storage.domain.FileResource;
import ecsimsw.picup.storage.domain.FileResourceRepository;
import ecsimsw.picup.storage.domain.ResourceKey;
import ecsimsw.picup.storage.service.FileResourceService;
import ecsimsw.picup.storage.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class DummyFileDeleteScenarioTest {

    @Autowired
    private FileResourceService fileResourceService;

    @MockBean
    private FileStorageService fileStorageService;

    @Autowired
    private FileDeletionService fileDeletionService;

    @Autowired
    private FileResourceRepository fileResourceRepository;

    private FileResource toBeDeleted;

    @BeforeEach
    void giveToBeDeleted() {
        toBeDeleted = new FileResource(STORAGE, new ResourceKey("resource.jpg"), 1L, true);
        fileResourceRepository.save(toBeDeleted);
    }

    @DisplayName("파일 삭제 제거 중 예외 발생시 재시도 처리한다.")
    @Nested
    class TestRetry {

        @BeforeEach
        void given() {
            doThrow(new IllegalArgumentException())
                .when(fileStorageService)
                .delete(any());
        }

        @DisplayName("스토리지에서 파일 제거 중 예외가 발생할 경우 N번 재시도 한다.")
        @Test
        void deleteRetry() {
            // when
            fileDeletionService.delete(toBeDeleted);

            // then
            verify(fileStorageService, times(FILE_DELETION_RETRY_COUNTS)).delete(any());
        }

        @DisplayName("재시도 처리 중 복구되는 경우, FileResource 는 DB 에서 제거한다.")
        @Test
        void retryFailed() {
            // given
            doThrow(new IllegalArgumentException())
                .doNothing()
                .when(fileStorageService).delete(any());

            // when
            fileDeletionService.delete(toBeDeleted);

            // then
            assertThat(fileResourceRepository.existsById(toBeDeleted.getId())).isFalse();
        }

    }

    @DisplayName("재시도에도 제거 실패시 복구 로직을 수행한다.")
    @Nested
    class TestRecover {

        @Autowired
        private FileDeletionFailedLogRepository failedLogRepository;

        @BeforeEach
        void given() {
            doThrow(new IllegalArgumentException())
                .when(fileStorageService)
                .delete(any());
        }

        @DisplayName("재시도 처리 후에도 처리가 실패하는 경우, FailedLog 를 DB 에 기록하고 FileResource 는 제거한다.")
        @Test
        void createFailedLog() {
            // given
            var beforeFailedLogCount = failedLogRepository.count();
            when(fileStorageService.hasContent(any()))
                .thenReturn(true);

            // when
            fileDeletionService.delete(toBeDeleted);

            // then
            assertThat(fileResourceRepository.existsById(toBeDeleted.getId())).isFalse();
            assertThat(failedLogRepository.count()).isNotEqualTo(beforeFailedLogCount);
        }

        @DisplayName("이미 스토리지에 File 이 존재하지 않는 경우, FailedLog 를 기록하지 않고 FileResource 는 제거한다.")
        @Test
        void fileAlreadyNotExists(@Autowired FileDeletionFailedLogRepository failedLogRepository) {
            // given
            var beforeFailedLogCount = failedLogRepository.count();
            when(fileStorageService.hasContent(any()))
                .thenReturn(false);

            // when
            fileDeletionService.delete(toBeDeleted);

            // then
            assertThat(fileResourceRepository.existsById(toBeDeleted.getId())).isFalse();
            assertThat(failedLogRepository.count()).isEqualTo(beforeFailedLogCount);
        }
    }

    @DisplayName("스토리지에서 파일을 제거한다.")
    @Nested
    class DeleteSuccess {
        @DisplayName("입력된 FileResource 를 스토리지에서 제거한다.")
        @Test
        void deleteFromStorage() {
            // when
            fileDeletionService.delete(toBeDeleted);

            // then
            var path = fileResourceService.filePath(toBeDeleted);
            verify(fileStorageService).delete(path);
        }

        @DisplayName("스토리지에서 정상 제거된 FileResource 는 DB 에서 제거한다.")
        @Test
        void deleteFromDB() {
            // when
            fileDeletionService.delete(toBeDeleted);

            // then
            assertThat(fileResourceRepository.existsById(toBeDeleted.getId())).isFalse();
        }
    }
}
