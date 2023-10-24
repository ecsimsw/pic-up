package ecsimsw.picup.domain;

import lombok.Getter;

@Getter
public enum HistoryType {

    CREATE("create"),
    DELETE("delete");

    private final String key;

    HistoryType(String key) {
        this.key = key;
    }
}
