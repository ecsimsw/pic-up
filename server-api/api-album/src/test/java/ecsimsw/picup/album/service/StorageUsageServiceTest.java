package ecsimsw.picup.album.service;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.domain.StorageUsage;
import ecsimsw.picup.album.domain.StorageUsageRepository;
import ecsimsw.picup.album.service.StorageUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class StorageUsageServiceTest {

    @Autowired
    private StorageUsageRepository storageUsageRepository;

    private StorageUsageService storageUsageService;
    private final Long userId = 1L;

    @BeforeEach
    public void init() {
        storageUsageService = new StorageUsageService(storageUsageRepository);
        storageUsageRepository.save(new StorageUsage(userId, Long.MAX_VALUE));
    }

    @DisplayName("유저별 스토리지 사용량을 저장한다.")
    @Test
    public void storeStorageUsage() {
        var fileSize = 256L;
        storageUsageService.addUsage(userId, fileSize);
        var usage = storageUsageService.getUsage(userId);
        assertThat(usage.getUsageAsByte()).isEqualTo(fileSize);
    }

    @DisplayName("최대 업로드 가능 사이즈를 넘어서 사진을 업로드 하는 경우 예외를 발생시킨다.")
    @Test
    public void overLimitUploadSize() {
        var usage = storageUsageService.getUsage(userId);
        var limit = usage.getLimitAsByte();
        assertThatThrownBy(
            () -> storageUsageService.addUsage(userId, limit)
        ).isInstanceOf(AlbumException.class);
    }

    @DisplayName("사진을 추가하는 경우 업로드한 사진의 사이즈 만큼 사용량이 증가한다")
    @Test
    public void addFileSize() {
        var uploadFileSize1 = 1;
        storageUsageService.addUsage(userId, uploadFileSize1);
        assertThat(storageUsageService.getUsage(userId).getUsageAsByte())
            .isEqualTo(uploadFileSize1);
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
