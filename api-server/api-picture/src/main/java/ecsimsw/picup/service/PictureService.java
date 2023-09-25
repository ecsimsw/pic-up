package ecsimsw.picup.service;

import ecsimsw.picup.domain.UserFileStoragePath;
import ecsimsw.picup.domain.UserFileStoragePathRepository;
import ecsimsw.picup.dto.PictureUploadRequest;
import ecsimsw.picup.dto.PictureUploadResponse;
import ecsimsw.picup.dto.StorageResourceResponse;
import ecsimsw.picup.dto.StorageResourceUploadResponse;
import ecsimsw.picup.dto.UserFileInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public PictureUploadResponse upload(Long folderId, PictureUploadRequest request) {
        final UserFileInfo userFile = userResourceService.createImage(folderId, request);
        final StorageResourceUploadResponse storageResource = fileStorageService.upload(request);

        final UserFileStoragePath userFileStoragePath = new UserFileStoragePath(userFile.getId(), storageResource.getKey());
        userFileStoragePathRepository.save(userFileStoragePath);

        return PictureUploadResponse.of(userFile, storageResource);
    }

    public PictureDownloadResponse downLoad(Long imageId) {
        final UserFileStoragePath userFileStoragePath = userFileStoragePathRepository.findByUserFileId(imageId).orElseThrow();
        final StorageResourceResponse storageResource = fileStorageService.download(userFileStoragePath.getResourceKey());
        return PictureDownloadResponse.of(storageResource);
    }
}
