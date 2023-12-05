package ecsimsw.picup.mq.message;

import ecsimsw.picup.storage.StorageKey;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileDeletionRequest {

    private String resourceKey;
    private StorageKey storageKey;

    public FileDeletionRequest() {
    }

    public FileDeletionRequest(String resourceKey, StorageKey storageKey) {
        this.resourceKey = resourceKey;
        this.storageKey = storageKey;
    }
}
