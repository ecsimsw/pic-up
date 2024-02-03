package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.StorageUsage;
import ecsimsw.picup.album.domain.StorageUsageRepository;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.storage.StorageUsageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StorageUsageService {

    private final StorageUsageRepository storageUsageRepository;

    @Transactional
    public void initNewUsage(StorageUsageDto storageUsageDto) {
        var storageUsage = new StorageUsage(storageUsageDto.getUserId(), storageUsageDto.getLimitAsByte(), 0L);
        storageUsageRepository.save(storageUsage);
    }

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
