package ecsimsw.picup.env;

import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;

public class StorageUsageMockRepository {

    public static Map<Long, StorageUsage> MOCK_DB;

    public static void init(StorageUsageRepository mockRepository) {
        MOCK_DB = new HashMap<>();
        lenient()
            .when(mockRepository.findByUserId(anyLong()))
            .thenAnswer(it -> StorageUsageMockRepository.findByUserId(it.getArgument(0, Long.class)));

        lenient()
            .when(mockRepository.save(any(StorageUsage.class)))
            .thenAnswer(it -> StorageUsageMockRepository.save(it.getArgument(0, StorageUsage.class)));
    }

    public static Optional<StorageUsage> findByUserId(Long userId) {
        return MOCK_DB.values().stream()
            .filter(it -> it.getUserId().equals(userId))
            .findAny();
    }

    public static StorageUsage save(StorageUsage storageUsage) {
        if (storageUsage.getId() != null) {
            MOCK_DB.put(storageUsage.getId(), storageUsage);
            return MOCK_DB.get(storageUsage.getId());
        }
        Long newId = MOCK_DB.values().stream()
            .mapToLong(StorageUsage::getId)
            .max()
            .orElse(0L) + 1;
        storageUsage.setId(newId);
        MOCK_DB.put(newId, storageUsage);
        return storageUsage;
    }
}
