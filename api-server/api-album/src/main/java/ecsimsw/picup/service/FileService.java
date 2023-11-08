package ecsimsw.picup.service;

import com.google.common.collect.Iterables;
import ecsimsw.picup.domain.FileExtension;
import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.dto.ImageFileInfo;
import ecsimsw.picup.exception.AlbumException;
import java.util.List;
import java.util.Objects;

import ecsimsw.picup.exception.MessageQueueServerDownException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    public final static int FILE_DELETION_SEGMENT_UNIT = 5;

    private final FileResourceRepository fileResourceRepository;
    private final StorageHttpClient storageHttpClient;
    private final StorageMessageQueue storageMessageQueue;

    public FileService(
        FileResourceRepository fileResourceRepository,
        StorageHttpClient storageHttpClient,
        StorageMessageQueue storageMessageQueue
    ) {
        this.fileResourceRepository = fileResourceRepository;
        this.storageHttpClient = storageHttpClient;
        this.storageMessageQueue = storageMessageQueue;
    }

    public FileResource upload(MultipartFile file, String tag) {
        final String fileName = file.getOriginalFilename();
        if (Objects.isNull(fileName) || !fileName.contains(".")) {
            throw new AlbumException("Invalid file name");
        }
        FileExtension.fromFileName(fileName);
        final ImageFileInfo imageFileInfo = storageHttpClient.requestUpload(file, tag);
        final FileResource createdResource = FileResource.created(imageFileInfo);
        fileResourceRepository.save(createdResource);
        return createdResource;
    }

    public void delete(String resourceKey) {
        deleteAll(List.of(resourceKey));
    }

    public void deleteAll(List<String> resourceKeys) {
        for(var keySegment : Iterables.partition(resourceKeys, FILE_DELETION_SEGMENT_UNIT)) {
            final List<FileResource> resources = fileResourceRepository.findAllByResourceKeyIn(keySegment);
            try {
                storageMessageQueue.pollDeleteRequest(keySegment);
                resources.forEach(FileResource::deleted);
                fileResourceRepository.saveAll(resources);
            } catch (MessageQueueServerDownException e) {
                resources.forEach(FileResource::markAsGarbage);
                fileResourceRepository.saveAll(resources);
            }
        }
    }
}
