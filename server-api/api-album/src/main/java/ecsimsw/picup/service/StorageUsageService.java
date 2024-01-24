package ecsimsw.picup.service;

import com.zaxxer.hikari.HikariPoolMXBean;
import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.exception.AlbumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageUsageService {

    private final StorageUsageRepository storageUsageRepository;

    public StorageUsageService(StorageUsageRepository storageUsageRepository) {
        this.storageUsageRepository = storageUsageRepository;
    }

    @Transactional
    public void initNewUsage(Long userId, Long limitAsByte) {
        final StorageUsage storageUsage = new StorageUsage(userId, limitAsByte, 0L);
        storageUsageRepository.save(storageUsage);
    }

    @Transactional(readOnly = true)
    public StorageUsage getUsage(Long userId) {
        return storageUsageRepository.getReferenceByUserId(userId);
    }

    Logger logger = LoggerFactory.getLogger(StorageUsageService.class);

    @Autowired
    HikariPoolMXBean hikariPoolMXBean;

//    @Retryable(
//        maxAttempts = 3,
//        backoff = @Backoff(delay = 500),
//        value = StaleStateException.class,
//        recover = "recoverAddUsageStaleStateException"
//    )

    @CacheEvict(value = "userAlbumFirstPageDefaultSize", key = "#userId")
//    @Retryable(
//        value = ObjectOptimisticLockingFailureException.class,
//        maxAttempts = 1,
//        backoff = @Backoff(delay = 500)
//    )
    @Transactional
    public void addUsage(Long userId, long fileSize) {
        var storageUsage = getUsage(userId);
        storageUsage.add(fileSize);
        storageUsageRepository.save(storageUsage);

        logger.info("\n"
            + "activeConnections : " + hikariPoolMXBean.getActiveConnections() + "\n"
            + "idleConnections : " + hikariPoolMXBean.getIdleConnections() + "\n"
            + "waitingConnections : " + hikariPoolMXBean.getThreadsAwaitingConnection()
        );

        System.out.println("여기까진 터지는데");
    }

    @Transactional
    public void subtractUsage(Long userId, long fileSize) {
        final StorageUsage usage = storageUsageRepository.findByUserId(userId)
            .orElseThrow(() -> new AlbumException("Invalid member id"));
        usage.subtract(fileSize);
        storageUsageRepository.save(usage);
    }
}
