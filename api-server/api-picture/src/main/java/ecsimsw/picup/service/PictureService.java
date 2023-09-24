package ecsimsw.picup.service;

import ecsimsw.picup.utils.FileReadResult;
import ecsimsw.picup.utils.FileWriteResult;
import ecsimsw.picup.utils.MultiPartFileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileAlreadyExistsException;

@Service
public class PictureService {

    private final String storageRootPath;

    public PictureService(@Value("${file.root.directory:./}") String storageRootPath) {
        this.storageRootPath = storageRootPath;
    }

    public FileWriteResult upload(MultipartFile multipartFile) {
        final String storagePath = storageRootPath + multipartFile.getName();
        return MultiPartFileUtils.write(storagePath, multipartFile);
    }

    public FileReadResult read(String path) {
        return MultiPartFileUtils.read(storageRootPath + path);
    }
}
