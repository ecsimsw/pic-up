package ecsimsw.picup;

import java.util.List;
import java.util.function.Function;

public record DataCsvFile(
    String fileName,
    List<String> columnNames,
    Function<Long, List<Object>> columnValues
) {
    public List<String> generateRow(Long id) {
        return columnValues.apply(id).stream()
            .map(String::valueOf)
            .toList();
    }

    public String columnNameLine(String delimiter) {
        return String.join(delimiter, columnNames);
    }

    public String columnValueLine(String delimiter, Long id) {
        var values = generateRow(id);
        return String.join(delimiter, values);
    }
}
