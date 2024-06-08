package ecsimsw.picup.service;

import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StorageUsageService {

    private final StorageUsageRepository storageUsageRepository;

    @Transactional
    public void init(Long userId, long limit) {
        var usage = new StorageUsage(userId, limit);
        storageUsageRepository.save(usage);
    }

    @Transactional(readOnly = true)
    public StorageUsage getUsage(Long userId) {
        return storageUsageRepository.findByUserId(userId)
            .orElseThrow(() -> new StorageException("Invalid memberId"));
    }

    @Transactional
    public void addUsage(Long userId, long fileSize) {
        var storageUsage = getUsage(userId);
        storageUsage.add(fileSize);
        storageUsageRepository.save(storageUsage);
    }

    @Transactional(readOnly = true)
    public boolean isAbleToStore(Long userId, long size) {
        return getUsage(userId).isAbleToStore(size);
    }

    @Transactional
    public void subtractAll(Long userId, long fileSize) {
        var usage = getUsage(userId);
        usage.subtract(fileSize);
        storageUsageRepository.save(usage);
    }

    @Transactional
    public void delete(Long userId) {
        storageUsageRepository.deleteByUserId(userId);
    }
}
