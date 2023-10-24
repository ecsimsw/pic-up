package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.ImageFileType;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.logging.CustomLogger;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.storage.LocalFileStorage;
import ecsimsw.picup.storage.S3ObjectStorage;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private static final CustomLogger LOGGER = CustomLogger.init(StorageService.class);

    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;

    public StorageService(
        LocalFileStorage localFileStorage,
        S3ObjectStorage s3ObjectStorage
    ) {
        this.mainStorage = localFileStorage;
        this.backUpStorage = s3ObjectStorage;
    }

    public ImageUploadResponse upload(MultipartFile file, String tag) {
        final String resourceKey = resourceKey(tag, file);
        final ImageFile imageFile = ImageFile.of(file);
        mainStorage.create(resourceKey, imageFile);
        try {
            backUpStorage.create(resourceKey, imageFile);
        } catch (Exception backUp) {
            try {
                mainStorage.delete(resourceKey);
                throw new StorageException("failed to upload to back up storage", backUp);
            } catch (Exception deletion) {
                // TODO :: 제거 실패 리소스 관리
                throw new StorageException("failed to delete from main storage", deletion);
            }
        }
        return new ImageUploadResponse(resourceKey, imageFile.getSize());
    }

    public ImageResponse read(String resourceKey) {
        validateResourceType(resourceKey);
        try {
            final ImageFile imageFile = mainStorage.read(resourceKey);
            return ImageResponse.of(imageFile);
        } catch (Exception e) {
            // TODO :: MAKE LOG
            final ImageFile imageFile = backUpStorage.read(resourceKey);
            return ImageResponse.of(imageFile);
        }
    }

    public void delete(String resourceKey) {
        try {
            mainStorage.delete(resourceKey);
        } catch (Exception e) {
            // TODO :: 제거 실패 리소스 관리
            LOGGER.error("Fail while deleting, resource key : " + resourceKey + " error message : " + e.getMessage());
        }
        try {
            backUpStorage.delete(resourceKey);
        } catch (Exception e) {
            // TODO :: 제거 실패 리소스 관리
            LOGGER.error("Fail while deleting, resource key : " + resourceKey + " error message : " + e.getMessage());
        }
    }

    public void deleteAll(List<String> resourceKeys) {
        resourceKeys.forEach(this::delete);
    }

    private String resourceKey(String fileTag, MultipartFile file) {
        final String originalName = file.getOriginalFilename();
        final String extension = originalName.substring(originalName.lastIndexOf(".") + 1);
        ImageFileType.extensionOf(extension);
        final String fileName = Strings.join(
            fileTag,
            UUID.randomUUID().toString()
        ).with("-");
        return fileName + "." + extension;
    }

    private void validateResourceType(String resourceKey) {
        ImageFileType.extensionOf(resourceKey);
    }
}
