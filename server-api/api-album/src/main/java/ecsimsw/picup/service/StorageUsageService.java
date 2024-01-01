package ecsimsw.picup.service;

import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.exception.AlbumException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StorageUsageService {

    private final StorageUsageRepository storageUsageRepository;

    public StorageUsageService(StorageUsageRepository storageUsageRepository) {
        this.storageUsageRepository = storageUsageRepository;
    }

    public StorageUsage getUsage(Long userId) {
        final Optional<StorageUsage> byUserId = storageUsageRepository.findByUserId(userId);
        if (byUserId.isPresent()) {
            return byUserId.orElseThrow();
        }
        return createNewStorageUsage(userId);
    }

    @Transactional
    public void addUsage(Long userId, long fileSize) {
        final Optional<StorageUsage> byUserId = storageUsageRepository.findByUserId(userId);
        if (byUserId.isEmpty()) {
            final StorageUsage storageUsage = createNewStorageUsage(userId);
            storageUsage.add(fileSize);
            return;
        }
        final StorageUsage storageUsage = byUserId.orElseThrow();
        storageUsage.add(fileSize);
        storageUsageRepository.save(storageUsage);
    }

    @Transactional
    public void subtractUsage(Long userId, long fileSize) {
        final StorageUsage usage = storageUsageRepository.findByUserId(userId)
            .orElseThrow(() -> new AlbumException("Invalid member id"));
        usage.subtract(fileSize);
        storageUsageRepository.save(usage);
    }

    private StorageUsage createNewStorageUsage(Long userId) {
        final StorageUsage storageUsage = new StorageUsage(userId, 10000000000L, 0L);
        return storageUsageRepository.save(storageUsage);
    }
}
