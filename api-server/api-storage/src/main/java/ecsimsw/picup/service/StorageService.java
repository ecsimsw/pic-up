package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.logging.CustomLogger;
import ecsimsw.picup.persistence.ImageStorage;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public void delete(String resourceKey) {
        mainImageStorage.delete(resourceKey);
    }

    public int deleteAll(List<String> resourceKeys) {
        int deletedCnt = 0;
        for (String resourceKey : resourceKeys) {
            try {
                mainImageStorage.delete(resourceKey);
                deletedCnt++;
            } catch (Exception e) {
                // TODO :: 제거 실패 리소스 관리, 예외 타입 구체화
                LOGGER.error("Fail while deleting, \nresource key : " + resourceKey + "\nerror message : " + e.getMessage());
            }
        }
        return deletedCnt;
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
