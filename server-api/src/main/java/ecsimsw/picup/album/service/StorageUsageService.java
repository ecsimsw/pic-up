package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.domain.StorageUsage;
import ecsimsw.picup.album.domain.StorageUsageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StorageUsageService {

    private static final Long DEFAULT_STORAGE_LIMIT_BYTE = Long.MAX_VALUE;

    private final StorageUsageRepository storageUsageRepository;

    @Transactional
    public StorageUsage init(Long userId, long limit) {
        var usage = new StorageUsage(userId, limit);
        return storageUsageRepository.save(usage);
    }

    @Transactional
    public StorageUsage init(Long userId) {
        return init(userId, DEFAULT_STORAGE_LIMIT_BYTE);
    }

    @Transactional(readOnly = true)
    public StorageUsage getUsage(Long userId) {
        return storageUsageRepository.findByUserId(userId)
            .orElseThrow(() -> new AlbumException("Invalid memberId"));
    }

    @Transactional
    public void addUsage(Long userId, long fileSize) {
        var storageUsage = getUsage(userId);
        storageUsage.add(fileSize);
        storageUsageRepository.save(storageUsage);
    }

    @Transactional
    public void subtractAll(Long userId, long fileSize) {
        var usage = getUsage(userId);
        usage.subtract(fileSize);
        storageUsageRepository.save(usage);
    }

    @Transactional
    public void subtractAll(Long userId, List<Picture> pictures) {
        var subtractUsageSum = pictures.stream()
            .mapToLong(Picture::getFileSize)
            .sum();
        var usage = getUsage(userId);
        usage.subtract(subtractUsageSum);
        storageUsageRepository.save(usage);
    }

    @Transactional
    public boolean isAbleToStore(Long userId, long fileSize) {
        return getUsage(userId)
            .isAbleToStore(fileSize);
    }
}
