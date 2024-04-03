package ecsimsw.picup.utils;

import static java.util.Collections.emptyList;

import java.util.List;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ListStorageKeyConverter implements AttributeConverter<List<String>, String> {

    private static final String SPLIT_DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<String> storageKeys) {
        if(storageKeys == null || storageKeys.isEmpty()) {
            return "";
        }
        return String.join(SPLIT_DELIMITER, storageKeys);
    }

    @Override
    public List<String> convertToEntityAttribute(String string) {
        if(string.isEmpty()) {
            return emptyList();
        }
        return List.of(string.split(SPLIT_DELIMITER));
    }
}
