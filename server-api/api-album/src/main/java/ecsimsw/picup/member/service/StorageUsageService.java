package ecsimsw.picup.member.service;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.member.domain.StorageUsage;
import ecsimsw.picup.member.domain.StorageUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StorageUsageService {

    private final StorageUsageRepository storageUsageRepository;

    @Transactional(readOnly = true)
    public StorageUsage getUsage(Long userId) {
        return storageUsageRepository.findByUserId(userId)
            .orElseThrow(() -> new AlbumException("Invalid member id"));
    }

    @Transactional
    public void addUsage(Long userId, long fileSize) {
        var storageUsage = getUsage(userId);
        storageUsage.add(fileSize);
        storageUsageRepository.save(storageUsage);
    }

    @Transactional
    public void subtractUsage(Long userId, long fileSize) {
        var usage = getUsage(userId);
        usage.subtract(fileSize);
        storageUsageRepository.save(usage);
    }
}
