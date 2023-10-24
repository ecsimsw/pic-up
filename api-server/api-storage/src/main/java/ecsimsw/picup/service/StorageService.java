package ecsimsw.picup.service;

import static ecsimsw.picup.domain.StorageKey.BACKUP_STORAGE;
import static ecsimsw.picup.domain.StorageKey.MAIN_STORAGE;

import ecsimsw.picup.domain.History;
import ecsimsw.picup.domain.HistoryRepository;
import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.ImageFileType;
import ecsimsw.picup.domain.Residue;
import ecsimsw.picup.domain.ResidueRepository;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.exception.FileNotExistsException;
import ecsimsw.picup.exception.StorageException;
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

    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;
    private final HistoryRepository historyRepository;
    private final ResidueRepository residueRepository;

    public StorageService(
        LocalFileStorage localFileStorage,
        S3ObjectStorage s3ObjectStorage,
        HistoryRepository historyRepository,
        ResidueRepository residueRepository
    ) {
        this.mainStorage = localFileStorage;
        this.backUpStorage = s3ObjectStorage;
        this.historyRepository = historyRepository;
        this.residueRepository = residueRepository;
    }

    public ImageUploadResponse upload(MultipartFile file, String tag) {
        final String resourceKey = resourceKey(tag, file);
        final ImageFile imageFile = ImageFile.of(file);

        mainStorage.create(resourceKey, imageFile);
        historyRepository.save(History.create(MAIN_STORAGE, resourceKey));
        try {
            backUpStorage.create(resourceKey, imageFile);
            historyRepository.save(History.create(BACKUP_STORAGE, resourceKey));
            return new ImageUploadResponse(resourceKey, imageFile.getSize());
        } catch (Exception e) {
            residueRepository.save(Residue.from(resourceKey, MAIN_STORAGE, e.getMessage()));
            throw new StorageException("Failed to back up", e);
        }
    }

    public ImageResponse read(String resourceKey) {
        ImageFileType.validateSupport(resourceKey);
        try {
            final ImageFile imageFile = mainStorage.read(resourceKey);
            return ImageResponse.of(imageFile);
        } catch (FileNotExistsException fileNotExistsException) {
            final ImageFile imageFile = backUpStorage.read(resourceKey);
            mainStorage.create(resourceKey, imageFile);
            historyRepository.save(History.create(MAIN_STORAGE, resourceKey, "Pull from backUp server while reading"));
            return ImageResponse.of(imageFile);
        }
    }

    public void deleteAll(List<String> resourceKeys) {
        resourceKeys.forEach(this::delete);
    }

    public void delete(String resourceKey) {
        delete(resourceKey, mainStorage);
        delete(resourceKey, backUpStorage);
    }

    public void delete(String resourceKey, ImageStorage storage) {
        try {
            storage.delete(resourceKey);
            historyRepository.save(History.delete(storage.key(), resourceKey));
        } catch (Exception e) {
            residueRepository.save(Residue.from(resourceKey, storage.key(), e.getMessage()));
        }
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
}
