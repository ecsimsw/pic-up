package ecsimsw.picup.service;

import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.exception.AlbumException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StorageUsageWriteService {

    private final StorageUsageRepository storageUsageRepository;

    @Transactional
    public void addUsage(StorageUsage storageUsage, long fileSize) {
        storageUsage.add(fileSize);
        storageUsageRepository.save(storageUsage);
    }
}
