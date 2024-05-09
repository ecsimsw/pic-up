package ecsimsw.picup.config.dev;

import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.domain.StorageType;
import ecsimsw.picup.album.service.FileResourceService;
import ecsimsw.picup.album.service.FileStorageService;
import ecsimsw.picup.album.service.FileUrlService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static ecsimsw.picup.album.domain.CacheType.SIGNED_URL;
import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.config.S3Config.ROOT_PATH_STORAGE;
import static ecsimsw.picup.config.S3Config.ROOT_PATH_THUMBNAIL;

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
