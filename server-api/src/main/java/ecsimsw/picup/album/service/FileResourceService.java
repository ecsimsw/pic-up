package ecsimsw.picup.album.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.exception.StorageException;
import ecsimsw.picup.storage.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileResourceService {

    private static final String BUCKET = "picup-ecsimsw";
    private static final Map<StorageType, String> ROOT_PATH_PER_STORAGE_TYPE = Map.of(
        StorageType.STORAGE, "storage/",
        THUMBNAIL, "thumb/"
    );

    private static final long PRE_SIGNED_URL_EXPIRATION_MS = 10_000;
    private static final int WAIT_TIME_TO_BE_DELETED = 10;
    private static final int FILE_DELETION_RETRY_COUNTS = 3;

    private final AmazonS3 s3Client;
    private final StorageResourceRepository storageResourceRepository;
    private final FileUrlService fileUrlService;
    private final ThumbnailService thumbnailService;

    @Transactional
    public ResourceKey upload(StorageType type, MultipartFile file, float scale) {
        var thumbnailFile = thumbnailService.resizeImage(file, scale);
        var resourceKey = ResourceKey.fromMultipartFile(thumbnailFile);
        S3Utils.store(s3Client, BUCKET, resourcePath(type, resourceKey), file);
        storageResourceRepository.save(new StorageResource(type, resourceKey, file.getSize()));
        return resourceKey;
    }

    @Transactional
    public String preUpload(StorageType type, String fileName, long fileSize) {
        var resourceKey = ResourceKey.fromFileName(fileName);
        var preUpload = StorageResource.preUpload(type, resourceKey, fileSize);
        storageResourceRepository.save(preUpload);
        return S3Utils.getPreSignedUrl(s3Client, BUCKET, resourcePath(preUpload), PRE_SIGNED_URL_EXPIRATION_MS);
    }

    @Transactional
    public StorageResource commitPreUpload(StorageType type, ResourceKey resourceKey) {
        var preUpload = storageResourceRepository.findByStorageTypeAndResourceKey(type, resourceKey)
            .orElseThrow(() -> new StorageException("There's nothing to commit"));
        preUpload.setToBeDeleted(false);
        storageResourceRepository.save(preUpload);
        return preUpload;
    }

    @Transactional
    public void saveStorageResource(StorageType type, ResourceKey resourceKey, long fileSize) {
        var storageResource = new StorageResource(type, resourceKey, fileSize);
        storageResourceRepository.save(storageResource);
    }

    @Transactional
    public void deleteAsync(ResourceKey resourceKey) {
        var resources = storageResourceRepository.findAllByResourceKey(resourceKey);
        resources.forEach(resource -> resource.setToBeDeleted(true));
        storageResourceRepository.saveAll(resources);
    }

    @Transactional
    public void deleteAllAsync(List<ResourceKey> resourceKeys) {
        var resources = storageResourceRepository.findAllByResourceKeyIn(resourceKeys);
        resources.forEach(resource -> resource.setToBeDeleted(true));
        storageResourceRepository.saveAll(resources);
    }

    @Transactional(readOnly = true)
    public void deleteAllDummies() {
        var expiration = LocalDateTime.now().minusSeconds(WAIT_TIME_TO_BE_DELETED);
        var toBeDeleted = storageResourceRepository.findAllCreatedBefore(expiration);
        toBeDeleted.forEach(resource -> {
            try {
                if (resource.getDeleteFailedCount() > FILE_DELETION_RETRY_COUNTS) {
                    // TODO :: DLQ
                    storageResourceRepository.delete(resource);
                    return;
                }
                S3Utils.delete(s3Client, BUCKET, resourcePath(resource));
                storageResourceRepository.delete(resource);
            } catch (Exception e) {
                resource.countDeleteFailed();
                storageResourceRepository.save(resource);
            }
        });
    }

    private String resourcePath(StorageResource resource) {
        return resourcePath(resource.getStorageType(), resource.getResourceKey());
    }

    private String resourcePath(StorageType type, ResourceKey resourceKey) {
        if (!ROOT_PATH_PER_STORAGE_TYPE.containsKey(type)) {
            return resourceKey.value();
        }
        return ROOT_PATH_PER_STORAGE_TYPE.get(type) + resourceKey.value();
    }

    public String fileUrl(StorageType type, String remoteIp, ResourceKey resourceKey) {
        var resourcePath = resourcePath(type, resourceKey);
        return fileUrlService.sign(remoteIp, resourcePath);
    }
}
