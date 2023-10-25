package ecsimsw.picup.service;

import ecsimsw.picup.domain.History;
import ecsimsw.picup.domain.HistoryRepository;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class StorageService {

    private final ResourceService resourceService;
    private final HistoryRepository historyRepository;

    public StorageService(ResourceService resourceService, HistoryRepository historyRepository) {
        this.resourceService = resourceService;
        this.historyRepository = historyRepository;
    }

    public ImageUploadResponse upload(MultipartFile file, String tag) {
        final ImageUploadResponse uploadResult = resourceService.upload(tag, file);
        historyRepository.save(History.create(uploadResult.getResourceKey()));
        return uploadResult;
    }

    public ImageResponse read(String resourceKey) {
        return resourceService.read(resourceKey);
    }

    public void delete(String resourceKey) {
        resourceService.delete(resourceKey);
        historyRepository.save(History.delete(resourceKey));
    }

    public void deleteAll(List<String> resourceKeys) {
        resourceKeys.forEach(this::delete);
    }
}
