package ecsimsw.picup.service;

import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.storage.StorageUsageDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageUsageService {

    private final StorageUsageRepository storageUsageRepository;

    public StorageUsageService(StorageUsageRepository storageUsageRepository) {
        this.storageUsageRepository = storageUsageRepository;
    }

    @Transactional
    public void initNewUsage(StorageUsageDto storageUsageDto) {
        final StorageUsage storageUsage = new StorageUsage(storageUsageDto.getUserId(), storageUsageDto.getLimitAsByte(), 0L);
        storageUsageRepository.save(storageUsage);
    }

    @Transactional(readOnly = true)
    public StorageUsage getUsage(Long userId) {
        return storageUsageRepository.findByUserId(userId)
            .orElseThrow(() -> new AlbumException("Usage for userId " + userId + " is not present"));
    }

    @Transactional
    public void addUsage(Long userId, long fileSize) {
        final StorageUsage storageUsage = storageUsageRepository.findByUserId(userId).orElseThrow();
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
}
