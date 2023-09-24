package ecsimsw.picup.utils;

import lombok.Getter;

import java.io.File;

@Getter
public class FileWriteResult {

    private final long fileSize;
    private final String fileName;
    private final String filePath;

    public FileWriteResult(long fileSize, String fileName, String filePath) {
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public static FileWriteResult of(File file) {
        return new FileWriteResult(file.length(), file.getName(), file.getPath());
    }
}
