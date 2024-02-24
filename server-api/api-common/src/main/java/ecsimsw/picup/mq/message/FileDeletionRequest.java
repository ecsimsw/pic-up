package ecsimsw.picup.mq.message;

import ecsimsw.picup.storage.StorageKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FileDeletionRequest {

    private String resourceKey;
    private StorageKey storageKey;
}
