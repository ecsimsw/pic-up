package ecsimsw.picup.storage.config.dev;

import ecsimsw.picup.storage.domain.ResourceKey;
import ecsimsw.picup.storage.domain.StorageType;
import ecsimsw.picup.storage.service.FileResourceService;
import ecsimsw.picup.storage.service.FileStorageService;
import ecsimsw.picup.storage.service.FileUrlService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static ecsimsw.picup.storage.config.CacheType.SIGNED_URL;
import static ecsimsw.picup.storage.config.S3Config.ROOT_PATH_STORAGE;
import static ecsimsw.picup.storage.config.S3Config.ROOT_PATH_THUMBNAIL;
import static ecsimsw.picup.storage.domain.StorageType.STORAGE;

@Primary
@Profile("dev")
@Service
public class MockFileUrlService extends FileUrlService {

    public MockFileUrlService(FileResourceService fileResourceService, FileStorageService fileStorageService) {
        super(fileResourceService, fileStorageService);
    }

    @Override
    @Cacheable(value = SIGNED_URL, key = "{#storageType, #remoteIp, #fileResource.value()}")
    public String fileUrl(StorageType storageType, String remoteIp, ResourceKey fileResource) {
        if (storageType == STORAGE) {
            return "http://localhost:8084/" + ROOT_PATH_STORAGE + fileResource.value();
        } else {
            return "http://localhost:8084/" + ROOT_PATH_THUMBNAIL + fileResource.value();
        }
    }
}
