package ecsimsw.picup.service;

import com.zaxxer.hikari.HikariPoolMXBean;
import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.exception.AlbumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        return storageUsageRepository.findByUserId(userId)
            .orElseThrow(() -> new AlbumException("Usage for userId " + userId + " is not present"));
    }

    Logger logger = LoggerFactory.getLogger(StorageUsageService.class);

    @Autowired
    HikariPoolMXBean hikariPoolMXBean;

    @Transactional
    public void addUsage(Long userId, long fileSize) {
        var bfActiveConn =  hikariPoolMXBean.getActiveConnections();
        var bfIdeConn =  hikariPoolMXBean.getIdleConnections();
        var bfWaitConn =  hikariPoolMXBean.getThreadsAwaitingConnection();

        var storageUsage = getUsage(userId);
        storageUsage.add(fileSize);
        storageUsageRepository.save(storageUsage);

        var afActiveConn =  hikariPoolMXBean.getActiveConnections();
        var afIdeConn =  hikariPoolMXBean.getIdleConnections();
        var afWaitConn =  hikariPoolMXBean.getThreadsAwaitingConnection();

        logger.info("\n"
            + "bf activeConnections : " + bfActiveConn + "\n"
            + "bf idleConnections : " + bfIdeConn+ "\n"
            + "bf waitingConnections : " + bfWaitConn + "\n"
            + "af activeConnections : " + afActiveConn + "\n"
            + "af idleConnections : " + afIdeConn+ "\n"
            + "af waitingConnections : " + afWaitConn
        );
    }

    @Transactional
    public void subtractUsage(Long userId, long fileSize) {
        final StorageUsage usage = storageUsageRepository.findByUserId(userId)
            .orElseThrow(() -> new AlbumException("Invalid member id"));
        usage.subtract(fileSize);
        storageUsageRepository.save(usage);
    }
}
