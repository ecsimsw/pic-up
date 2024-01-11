package ecsimsw.picup.service;

import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.exception.AlbumException;
import org.hibernate.StaleStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageUsageService {

    private final StorageUsageRepository storageUsageRepository;

    public StorageUsageService(StorageUsageRepository storageUsageRepository) {
        this.storageUsageRepository = storageUsageRepository;
    }

    @Autowired
    StorageUsageWriteService storageUsageWriteService;

    @Autowired
    StorageUsageReadService storageUsageReadService;

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

    @Transactional
    public void addUsage(Long userId, long fileSize) {
        final long start = System.currentTimeMillis();
//        System.out.println("hi");
        var storageUsage = storageUsageReadService.getUsage(userId);
        storageUsageWriteService.addUsage(storageUsage, fileSize);
//        storageUsage.add(fileSize);
//        storageUsageRepository.save(storageUsage);
        System.out.println("FFFFFFFFF : "+ (System.currentTimeMillis() - start));
    }

    @Transactional
    public void subtractUsage(Long userId, long fileSize) {
        final StorageUsage usage = storageUsageRepository.findByUserId(userId)
            .orElseThrow(() -> new AlbumException("Invalid member id"));
        usage.subtract(fileSize);
        storageUsageRepository.save(usage);
    }
}
