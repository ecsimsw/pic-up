package ecsimsw.picup.service;

import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.env.StorageUsageMockRepository;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.storage.StorageUsageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class StorageUsageServiceTest {

    @Mock
    private StorageUsageRepository storageUsageRepository;

    private StorageUsageService storageUsageService;

    private Long userId = 1L;

    private StorageUsageDto storageUsage = new StorageUsageDto(userId, 10000L);

    @BeforeEach
    public void init() {
        storageUsageService = new StorageUsageService(storageUsageRepository);
        StorageUsageMockRepository.init(storageUsageRepository);

        storageUsageService.initNewUsage(storageUsage);
    }

    @DisplayName("유저별 스토리지 사용량을 저장한다.")
    @Test
    public void storeStorageUsage() {
        var fileSize = 256L;

        storageUsageService.initNewUsage(storageUsage);
        storageUsageService.addUsage(userId, fileSize);

        var usage = storageUsageService.getUsage(userId);
        assertThat(usage.getUsageAsByte()).isEqualTo(fileSize);
    }

    @DisplayName("최대 업로드 가능 사이즈를 넘어서 사진을 업로드 하는 경우 예외를 발생시킨다.")
    @Test
    public void overLimitUploadSize() {
        storageUsageService.initNewUsage(storageUsage);
        var usage = storageUsageService.getUsage(userId);
        var limit = usage.getLimitAsByte();
        var uploadFileSize = limit + 1;

        assertThatThrownBy(
            () -> storageUsageService.addUsage(userId, uploadFileSize)
        ).isInstanceOf(AlbumException.class);
    }

    @DisplayName("사진을 추가하는 경우 업로드한 사진의 사이즈 만큼 사용량이 증가한다")
    @Test
    public void addFileSize() {
        var uploadFileSize1 = 1;
        storageUsageService.addUsage(userId, uploadFileSize1);
        assertThat(storageUsageService.getUsage(userId).getUsageAsByte())
            .isEqualTo(uploadFileSize1);

        var uploadFileSize2 = 10;
        storageUsageService.addUsage(userId, uploadFileSize2);
        assertThat(storageUsageService.getUsage(userId).getUsageAsByte())
            .isEqualTo(uploadFileSize1 + uploadFileSize2);
    }

    @DisplayName("사진을 제거하는 경우 제거한 사진의 사이즈 만큼 사용량이 감소한다")
    @Test
    public void subtractFileSize() {
        var initialFileSize = 100;
        storageUsageService.addUsage(userId, initialFileSize);

        var deleteFileSize1 = 1;
        storageUsageService.subtractUsage(userId, deleteFileSize1);
        assertThat(storageUsageService.getUsage(userId).getUsageAsByte())
            .isEqualTo(initialFileSize - deleteFileSize1);

        var deleteFileSize2 = 10;
        storageUsageService.subtractUsage(userId, deleteFileSize2);
        assertThat(storageUsageService.getUsage(userId).getUsageAsByte())
            .isEqualTo(initialFileSize - deleteFileSize1 - deleteFileSize2);
    }

    @DisplayName("감소한 사용량이 0보다 작은 경우 사용량을 0으로 초기화한다.")
    @Test
    public void subtractFileSizeUnderZero() {
        var initialFileSize = 100;
        storageUsageService.addUsage(userId, initialFileSize);

        var deleteFileSize = initialFileSize + 1;
        storageUsageService.subtractUsage(userId, deleteFileSize);

        assertThat(storageUsageService.getUsage(userId).getUsageAsByte())
            .isEqualTo(0);
    }
}
