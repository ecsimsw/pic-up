package ecsimsw.picup.album.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.controller.PreUploadResponse;
import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.exception.StorageException;
import ecsimsw.picup.album.utils.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static ecsimsw.picup.config.S3Config.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileStorageService {

    private static final int WAIT_TIME_TO_BE_DELETED = 10;
    private static final int FILE_DELETION_RETRY_COUNTS = 3;

    private final AmazonS3 s3Client;
    private final StorageResourceRepository storageResourceRepository;
    private final ThumbnailService thumbnailService;
    private final FileDeletionFailedHistoryRepository fileDeletionFailedHistoryRepository;

    @Transactional
    public ResourceKey upload(StorageType type, MultipartFile file, float scale) {
        var thumbnailFile = thumbnailService.resizeImage(file, scale);
        var resourceKey = ResourceKey.fromMultipartFile(thumbnailFile);
        S3Utils.store(s3Client, BUCKET, resourcePath(type, resourceKey), file);
        storageResourceRepository.save(new StorageResource(type, resourceKey, file.getSize()));
        return resourceKey;
    }

    @Transactional
    public PreUploadResponse preUpload(StorageType type, String fileName, long fileSize) {
        var resourceKey = ResourceKey.fromFileName(fileName);
        var preUpload = StorageResource.preUpload(type, resourceKey, fileSize);
        storageResourceRepository.save(preUpload);
        var resourceUrl = resourcePath(preUpload);
        var preSignedUrl = preSignedUrl(resourceUrl);
        System.out.println("preSigned : " + preSignedUrl);
        return new PreUploadResponse(preSignedUrl, resourceKey.value());
    }

    @Transactional
    public StorageResource commitPreUpload(StorageType type, ResourceKey resourceKey) {
        var preUpload = storageResourceRepository.findByStorageTypeAndResourceKey(type, resourceKey)
            .orElseThrow(() -> new StorageException("Not exists resource"));
        preUpload.setToBeDeleted(false);
        storageResourceRepository.save(preUpload);
        return preUpload;
    }

    @Transactional
    public void saveResource(StorageType type, ResourceKey resourceKey, long fileSize) {
        var storageResource = new StorageResource(type, resourceKey, fileSize);
        storageResourceRepository.save(storageResource);
    }

    @Transactional
    public void deleteAsync(ResourceKey resource) {
        deleteAllAsync(List.of(resource));
    }

    @Transactional
    public void deleteAllAsync(List<ResourceKey> resourceKeys) {
        storageResourceRepository.updateAllToBeDeleted(resourceKeys);
    }

    @Transactional
    public void deleteAllDummies() {
        var expiration = LocalDateTime.now().minusSeconds(WAIT_TIME_TO_BE_DELETED);
        var toBeDeleted = storageResourceRepository.findAllToBeDeletedCreatedBefore(expiration);
        for(var resource : toBeDeleted) {
            deleteDummyFile(resource);
        }
    }

    private void deleteDummyFile(StorageResource resource) {
        try {
            S3Utils.delete(s3Client, BUCKET, resourcePath(resource));
            storageResourceRepository.delete(resource);
        } catch (Exception e) {
            resource.countDeleteFailed();
            storageResourceRepository.save(resource);
            if (resource.getDeleteFailedCount() > FILE_DELETION_RETRY_COUNTS) {
                if(!S3Utils.hasContent(s3Client, BUCKET, resourcePath(resource))) {
                    fileDeletionFailedHistoryRepository.save(FileDeletionFailedHistory.from(resource));
                    log.error("Failed to delete file resource : " + resource.getResourceKey().value() + " " + resource.getStorageType().name());
                }
                storageResourceRepository.delete(resource);
            }
        }
    }

    private String resourcePath(StorageResource resource) {
        return resourcePath(resource.getStorageType(), resource.getResourceKey());
    }

    public String resourcePath(StorageType type, ResourceKey resourceKey) {
        if (!ROOT_PATH_PER_STORAGE_TYPE.containsKey(type)) {
            return resourceKey.value();
        }
        return ROOT_PATH_PER_STORAGE_TYPE.get(type) + resourceKey.value();
    }

    public String preSignedUrl(String resourceUrl) {
        return S3Utils.preSignedUrl(s3Client, BUCKET, resourceUrl, PRE_SIGNED_URL_EXPIRATION_MS * 100);
    }
}
