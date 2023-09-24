package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.StoragePath;
import ecsimsw.picup.dto.ImageLoadResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.storage.LocalImageStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PictureService {

    private final ImageStorage imageStorage;

    public PictureService(LocalImageStorage imageStorage) {
        this.imageStorage = imageStorage;
    }

    public ImageUploadResponse upload(Long folderId, MultipartFile multipartFile) {
        final StoragePath path = StoragePath.of("username", folderId, multipartFile.getName());
        final ImageFile imageFile = ImageFile.of(multipartFile);
        imageStorage.create(path, imageFile);
        return ImageUploadResponse.of(imageFile, folderId);
    }

    public ImageLoadResponse load(StoragePath path) {
        final ImageFile imageFile = imageStorage.read(path);
        return ImageLoadResponse.of(imageFile);
    }
}
