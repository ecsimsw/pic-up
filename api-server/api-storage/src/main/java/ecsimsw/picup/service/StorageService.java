package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.logging.CustomLogger;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private static final CustomLogger LOGGER = CustomLogger.init(StorageService.class);

    private final ImageStorage mainImageStorage;

    public StorageService(ImageStorage mainImageStorage) {
        this.mainImageStorage = mainImageStorage;
    }

    public ImageUploadResponse upload(MultipartFile file, String tag) {
        final String resourceKey = resourceKey(tag, file);
        final ImageFile imageFile = ImageFile.of(file);
        mainImageStorage.create(resourceKey, imageFile);
        return new ImageUploadResponse(resourceKey, imageFile.getSize());
    }

    public byte[] read(String resourceKey) {
        final ImageFile read = mainImageStorage.read(resourceKey);
        return read.getFile();
    }

    public void delete(String resourceKey) {
        mainImageStorage.delete(resourceKey);
    }

    public List<String> deleteAll(List<String> resourceKeys) {
        final List<String> deleted = new LinkedList<>();
        for (String resourceKey : resourceKeys) {
            try {
                mainImageStorage.delete(resourceKey);
                deleted.add(resourceKey);
            } catch (InvalidResourceException e) {
                // TODO :: 제거 실패 리소스 관리
                LOGGER.error("Fail while deleting, resource key : " + resourceKey + " error message : " + e.getMessage());
            }
        }
        return deleted;
    }

    private String resourceKey(String fileTag, MultipartFile file) {
        final String originalName = file.getOriginalFilename();
        final String extension = originalName.substring(originalName.lastIndexOf(".") + 1);
        final String fileName = Strings.join(
            fileTag,
            UUID.randomUUID().toString()
        ).with("-");
        return fileName + "." + extension;
    }
}
