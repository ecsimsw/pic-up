package ecsimsw.picup.member.service;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.member.domain.StorageUsage;
import ecsimsw.picup.member.domain.StorageUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@Service
public class StorageUsageService {

    private final StorageUsageLock storageUsageLock;
    private final StorageUsageRepository storageUsageRepository;

    @Transactional(readOnly = true)
    public StorageUsage getUsage(Long userId) {
        return storageUsageRepository.findByUserId(userId)
            .orElseThrow(() -> new AlbumException("Invalid member id"));
    }

    @Transactional
    public void addUsage(Long userId, long fileSize) {
        try {
            storageUsageLock.acquire(userId);
            var storageUsage = getUsage(userId);
            storageUsage.add(fileSize);
            storageUsageRepository.save(storageUsage);
        } catch (TimeoutException e) {
            throw new IllegalArgumentException("Lock wait time out");
        } finally {
            storageUsageLock.release(userId);
        }
    }

    @Transactional
    public void subtractUsage(Long userId, long fileSize) {
        try {
            storageUsageLock.acquire(userId);
            var usage = getUsage(userId);
            usage.subtract(fileSize);
            storageUsageRepository.save(usage);
        } catch (TimeoutException e) {
            throw new IllegalArgumentException("Lock time out");
        } finally {
            storageUsageLock.release(userId);
        }
    }
}
