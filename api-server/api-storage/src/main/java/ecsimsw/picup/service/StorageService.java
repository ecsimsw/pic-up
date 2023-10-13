package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.persistence.ImageStorage;
import java.util.UUID;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private final ImageStorage mainImageStorage;

    public StorageService(ImageStorage mainImageStorage) {
        this.mainImageStorage = mainImageStorage;
    }

    @Transactional
    public ImageUploadResponse upload(MultipartFile file, String tag) {
        final String resourceKey = resourceKey(tag, file);
        final ImageFile imageFile = ImageFile.of(file);
        mainImageStorage.create(resourceKey, imageFile);
        return new ImageUploadResponse(resourceKey, imageFile.getSize());
    }

    @Transactional
    public void delete(String resourceKey) {
        mainImageStorage.delete(resourceKey);
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
