package ecsimsw.picup.service;

import ecsimsw.picup.domain.UserFileStoragePath;
import ecsimsw.picup.domain.UserFileStoragePathRepository;
import ecsimsw.picup.dto.PictureUploadRequest;
import ecsimsw.picup.dto.PictureUploadResponse;
import ecsimsw.picup.dto.StorageResourceUploadResponse;
import ecsimsw.picup.dto.UserFileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PictureService {

    private final FileStorageService fileStorageService;
    private final UserResourceService userResourceService;
    private final UserFileStoragePathRepository userFileStoragePathRepository;

    public PictureService(
        FileStorageService fileStorageService,
        UserResourceService userResourceService,
        UserFileStoragePathRepository userFileStoragePathRepository
    ) {
        this.fileStorageService = fileStorageService;
        this.userResourceService = userResourceService;
        this.userFileStoragePathRepository = userFileStoragePathRepository;
    }

    public PictureUploadResponse upload(Long folderId, PictureUploadRequest request) {
        final UserFileInfo userFile = userResourceService.createImage(folderId, request);
        final StorageResourceUploadResponse storageResource = fileStorageService.upload(request);

        final UserFileStoragePath userFileStoragePath = new UserFileStoragePath(userFile.getId(), storageResource.getKey());
        userFileStoragePathRepository.save(userFileStoragePath);

        return PictureUploadResponse.of(userFile, storageResource);
    }
}
