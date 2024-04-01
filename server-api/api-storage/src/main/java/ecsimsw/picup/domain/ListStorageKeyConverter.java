package ecsimsw.picup.domain;

import static java.util.Collections.emptyList;

import ecsimsw.picup.storage.StorageKey;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ListStorageKeyConverter implements AttributeConverter<List<StorageKey>, String> {

    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(List<StorageKey> storageKeys) {
        if(storageKeys == null || storageKeys.isEmpty()) {
            return "";
        }
        return storageKeys.stream()
            .map(Enum::name)
            .collect(Collectors.joining(SPLIT_CHAR));
    }

    @Override
    public List<StorageKey> convertToEntityAttribute(String string) {
        if(string.isEmpty()) {
            return emptyList();
        }
        return Arrays.stream(string.split(SPLIT_CHAR))
            .map(StorageKey::valueOf)
            .toList();
    }
}
