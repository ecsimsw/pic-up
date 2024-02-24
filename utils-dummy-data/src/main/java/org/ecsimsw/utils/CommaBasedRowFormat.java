package org.giggles.utils;

import java.util.List;

public abstract class CommaBasedRowFormat implements RowFormatStrategy {

    private final List<String> columnNames;

    public CommaBasedRowFormat(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    @Override
    public String columnNameLine() {
        return String.join(",", columnNames);
    }

    @Override
    public String row(long id) {
        return String.join(",", randomColumnValues(id));
    }
}
