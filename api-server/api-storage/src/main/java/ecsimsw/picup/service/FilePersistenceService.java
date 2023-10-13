package ecsimsw.picup.service;

import ecsimsw.picup.dto.StorageResourceInfo;
import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.persistence.ImageStorage;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class FilePersistenceService {

    private final ImageStorage mainImageStorage;

    public FilePersistenceService(ImageStorage mainImageStorage) {
        this.mainImageStorage = mainImageStorage;
    }

    @Transactional
    public StorageResourceInfo upload(MultipartFile file, String fileTag) {
        final String resourceKey = resourceKey(fileTag, file);
        final ImageFile imageFile = ImageFile.of(file);
        mainImageStorage.create(resourceKey, imageFile);
        return StorageResourceInfo.of(imageFile, resourceKey);
    }

    @Transactional(readOnly = true)
    public StorageResourceInfo download(String resourceKey) {
        final ImageFile imageFile = mainImageStorage.read(resourceKey);
        return StorageResourceInfo.of(imageFile, resourceKey);
    }

    @Transactional
    public void delete(String resourceKey) {
        mainImageStorage.delete(resourceKey);
    }

    @Transactional
    public void deleteAll(List<String> resourceKeys) {
        resourceKeys.forEach(this::delete);
    }

    private String resourceKey(String fileTag, MultipartFile file) {
        final String fileOriginalName = file.getOriginalFilename();
        return Strings.join(
            fileTag,
            UUID.randomUUID().toString(),
            "."+fileOriginalName.substring(fileOriginalName.lastIndexOf(".") + 1)
        ).with("-");
    }
}
