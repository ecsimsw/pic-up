package org.ecsimsw.utils;

public interface RowFormatStrategy {

    String row(long id);

    String columnNameLine();

    Iterable<? extends CharSequence> randomColumnValues(long id);
}
