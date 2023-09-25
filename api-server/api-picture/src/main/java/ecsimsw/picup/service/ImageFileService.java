package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.StoragePath;
import ecsimsw.picup.domain.UserImageResource;
import ecsimsw.picup.domain.UserImageResourceRepository;
import ecsimsw.picup.dto.ImageLoadResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.storage.ImageStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageFileService {

    private final ImageStorage imageStorage;
    private final UserImageResourceRepository imageResourceRepository;

    public ImageFileService(
        ImageStorage imageStorage,
        UserImageResourceRepository imageResourceRepository
    ) {
        this.imageStorage = imageStorage;
        this.imageResourceRepository = imageResourceRepository;
    }

    public ImageUploadResponse upload(Long parentFolderId, MultipartFile multipartFile) {
        final StoragePath path = StoragePath.of("username", parentFolderId, multipartFile.getName());
        final ImageFile imageFile = ImageFile.of(multipartFile);
        imageStorage.create(path, imageFile);
        return ImageUploadResponse.of(imageFile, parentFolderId);
    }

    public ImageLoadResponse load(StoragePath path) {
        final ImageFile imageFile = imageStorage.read(path);
        return ImageLoadResponse.of(imageFile);
    }

    public void delete(Long imageId) {
        final UserImageResource imageResource = imageResourceRepository.findById(imageId).orElseThrow();
        imageStorage.delete(imageResource.getStoragePath());
    }
}
