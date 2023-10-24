package ecsimsw.picup.domain;

import lombok.Getter;

@Getter
public enum StorageKey {

    MAIN_STORAGE("main"),
    BACKUP_STORAGE("backUp");

    private final String key;

    StorageKey(String key) {
        this.key = key;
    }
}
