package org.ecsimsw.utils;

public record DataFileInfo(
    String fileName,
    long fileSize,
    long dataCount,
    long totalTime
) {

    public long fileSizeAsMB() {
        return fileSize / 1024 / 1024;
    }

    public long totalTimeAsSec() {
        return totalTime / 1000;
    }
}