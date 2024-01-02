package ecsimsw.picup.service;

import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.ecrypt.EncryptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(locations = "/databaseConfig.properties")
@DataJpaTest
public class StorageUsageSynchronizedTest {

    private final int numberOfThreads = 2;

    private final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    private final CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);

    @MockBean
    private EncryptService encryptService;

    @Autowired
    private StorageUsageRepository storageUsageRepository;

    private StorageUsageService storageUsageService;

    @BeforeEach
    public void init() {
        storageUsageService = new StorageUsageService(storageUsageRepository);
    }

    @DisplayName("100개의 추가, 제거가 동시에 진행될 때 최종 결과의 일관성을 확인한다.")
    @Test
    public void syncTest1() throws InterruptedException {
        Long userId = 1L;
        storageUsageService.initNewUsage(userId, 10000000000L);
        storageUsageService.addUsage(userId, 100);

        final Optional<StorageUsage> byUserId = storageUsageRepository.findByUserId(userId);
        System.out.println("hihi" + byUserId.get().getUsageAsByte());

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                final Optional<StorageUsage> dd = storageUsageRepository.findByUserId(userId);
                System.out.println(dd.get());

                storageUsageService.addUsage(userId, 1000);
                storageUsageService.subtractUsage(userId, 1000);
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        assertThat(storageUsageService.getUsage(userId).getUsageAsByte())
            .isEqualTo(100L);
    }
}
