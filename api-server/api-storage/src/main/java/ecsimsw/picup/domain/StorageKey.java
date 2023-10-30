package ecsimsw.picup.domain;

import lombok.Getter;

@Getter
public enum StorageKey {

    LOCAL_FILE_STORAGE,
    S3_OBJECT_STORAGE,
    NONE
}
