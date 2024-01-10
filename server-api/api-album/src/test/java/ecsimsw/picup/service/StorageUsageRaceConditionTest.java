package ecsimsw.picup.service;

import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.ecrypt.EncryptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//@Transactional(propagation = Propagation.NOT_SUPPORTED)

@TestPropertySource(locations = "/databaseConfig.properties")
@DataJpaTest
public class StorageUsageRaceConditionTest {

    @MockBean
    EncryptService encryptService;

    @Autowired
    private StorageUsageRepository storageUsageRepository;

    private StorageUsageService storageUsageService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void init() {
        storageUsageService = new StorageUsageService(storageUsageRepository);
    }

    @Test
    public void test() throws InterruptedException {
        int vuSize = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(vuSize);
        CountDownLatch countDownLatch = new CountDownLatch(vuSize);
        storageUsageService.initNewUsage(1L, 100L);

        storageUsageRepository.saveAndFlush(new StorageUsage(1L, 1000L, 0L));
//        storageUsageService.addUsage(1L, 20);
//        var usage1 = storageUsageService.getUsage(1L);
//        System.out.println("hi" + usage1.getUsageAsByte());
//
//        storageUsageService.addUsage(1L, 20);
//        var usage2 = storageUsageService.getUsage(1L);
//        System.out.println("hi" + usage2.getUsageAsByte());
//
//        storageUsageService.addUsage(1L, 20);
//        var usage3 = storageUsageService.getUsage(1L);
//        System.out.println("hi" + usage3.getUsageAsByte());
//
//        storageUsageService.addUsage(1L, 20);
//        var usage4 = storageUsageService.getUsage(1L);
//        System.out.println("hi" + usage4.getUsageAsByte());
//
//        storageUsageService.addUsage(1L, 20);
//        var usage5 = storageUsageService.getUsage(1L);
//        System.out.println("hi" + usage5.getUsageAsByte());
//

        System.out.println("start");
        for (int i = 0; i < vuSize; i++) {
            executorService.execute(() -> {
                storageUsageService.addUsage(1L, 20);
                var usage = storageUsageService.getUsage(1L);
                System.out.println("hi" + usage.getUsageAsByte());
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.out.println("end");
    }
}
