package ecsimsw.picup.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import ecsimsw.picup.logging.CustomLogger;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private static final CustomLogger LOGGER = CustomLogger.init(FileService.class);

    private static final int IMAGE_UPLOAD_RETRY_COUNT = 2;
    private static final int IMAGE_DELETE_ALL_RETRY_COUNT = 2;
    private static final int IMAGE_DELETE_ALL_API_CALL_SEG_UNIT = 5;

    private final StorageHttpClient storageClient;

    public FileService(StorageHttpClient storageClient) {
        this.storageClient = storageClient;
    }

    public String upload(MultipartFile file, String tag) {
        return upload(file, tag, IMAGE_UPLOAD_RETRY_COUNT);
    }

    public String upload(MultipartFile file, String tag, int leftRetryCnt) {
        var response = storageClient.requestUpload(file, tag);
        if(response.getResourceKey() != null) {
            return response.getResourceKey();
        }
        if(leftRetryCnt > 0) {
            return upload(file, tag, leftRetryCnt-1);
        }
        // TODO :: poll in queue
        LOGGER.error("Failed to upload resources : " + tag + " file size : " + file.getSize());
        throw new IllegalArgumentException("Failed to upload resources");
    }

    public void delete(String resourceKey) {
        deleteAll(List.of(resourceKey));
    }

    public void deleteAll(List<String> resources) {
        deleteAll(resources, IMAGE_DELETE_ALL_RETRY_COUNT);
    }

    private void deleteAll(List<String> resources, int leftRetryCnt) {
        final List<String> toBeRetried = new ArrayList<>();
        for (var resourcePart : Iterables.partition(resources, IMAGE_DELETE_ALL_API_CALL_SEG_UNIT)) {
            var deleted = storageClient.requestDelete(resourcePart);
            var failed = new ArrayList<>(Sets.difference(Sets.newHashSet(resourcePart), Sets.newHashSet(deleted)));
            toBeRetried.addAll(failed);
        }
        if(toBeRetried.isEmpty()) {
            return;
        }
        if(leftRetryCnt > 0) {
            deleteAll(toBeRetried, leftRetryCnt-1);
            return;
        }
        // TODO :: poll in queue
        LOGGER.error("Failed to delete resources : " + resources.size());
        throw new IllegalArgumentException("Failed to delete resources");
    }
}
