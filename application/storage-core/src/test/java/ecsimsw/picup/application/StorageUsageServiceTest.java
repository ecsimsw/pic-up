package ecsimsw.picup.application;

import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.service.StorageUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ecsimsw.picup.utils.AlbumFixture.USER_ID;

@ActiveProfiles(value = {"storage-core-dev"})
@DataJpaTest
public class StorageUsageServiceTest {

    private StorageUsageService storageUsageService;

    @BeforeEach
    public void init(@Autowired StorageUsageRepository storageUsageRepository) {
        storageUsageService = new StorageUsageService(storageUsageRepository);
        storageUsageRepository.save(new StorageUsage(USER_ID, Long.MAX_VALUE));
    }

    @DisplayName("유저별 스토리지 사용량을 저장한다.")
    @Test
    public void storeStorageUsage() {
        var fileSize = 256L;
        storageUsageService.addUsage(USER_ID, fileSize);
        var usage = storageUsageService.getUsage(USER_ID);
        assertThat(usage.getUsageAsByte()).isEqualTo(fileSize);
    }

    @DisplayName("최대 업로드 가능 사이즈를 넘어서 사진을 업로드 하는 경우 예외를 발생시킨다.")
    @Test
    public void overLimitUploadSize() {
        var usage = storageUsageService.getUsage(USER_ID);
        var limit = usage.getLimitAsByte();
        assertThatThrownBy(
            () -> storageUsageService.addUsage(USER_ID, limit)
        ).isInstanceOf(StorageException.class);
    }

    @DisplayName("사진을 추가하는 경우 업로드한 사진의 사이즈 만큼 사용량이 증가한다")
    @Test
    public void addFileSize() {
        var uploadFileSize1 = 1;
        storageUsageService.addUsage(USER_ID, uploadFileSize1);
        assertThat(storageUsageService.getUsage(USER_ID).getUsageAsByte())
            .isEqualTo(uploadFileSize1);
    }

    @DisplayName("사진을 제거하는 경우 제거한 사진의 사이즈 만큼 사용량이 감소한다")
    @Test
    public void subtractFileSize() {
        var initialFileSize = 100;
        storageUsageService.addUsage(USER_ID, initialFileSize);

        var deleteFileSize1 = 1;
        storageUsageService.subtractAll(USER_ID, deleteFileSize1);
        assertThat(storageUsageService.getUsage(USER_ID).getUsageAsByte())
            .isEqualTo(initialFileSize - deleteFileSize1);
    }

    @DisplayName("감소한 사용량이 0보다 작은 경우 사용량을 0으로 초기화한다.")
    @Test
    public void subtractFileSizeUnderZero() {
        var initialFileSize = 100;
        storageUsageService.addUsage(USER_ID, initialFileSize);

        var deleteFileSize = initialFileSize + 1;
        storageUsageService.subtractAll(USER_ID, deleteFileSize);

        assertThat(storageUsageService.getUsage(USER_ID).getUsageAsByte())
            .isEqualTo(0);
    }
}
