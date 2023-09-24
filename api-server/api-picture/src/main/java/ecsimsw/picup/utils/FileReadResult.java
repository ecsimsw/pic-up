package ecsimsw.picup.utils;

import lombok.Getter;

import java.io.File;

@Getter
public class FileReadResult {

    private final long fileSize;
    private final String fileName;
    private final String filePath;
    private final byte[] binaryValue;

    public FileReadResult(long fileSize, String fileName, String filePath, byte[] binaryValue) {
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.filePath = filePath;
        this.binaryValue = binaryValue;
    }

    public static FileReadResult of(File file, byte[] binaryValue) {
        return new FileReadResult(file.length(), file.getName(), file.getPath(), binaryValue);
    }
}
