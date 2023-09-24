package ecsimsw.picup.service;

import ecsimsw.picup.dto.FileUploadResult;
import ecsimsw.picup.utils.MultiPartFileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PictureService {

    private final String uploadDir;

    public PictureService(@Value("${file.root.directory:./}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public FileUploadResult fileUpload(MultipartFile multipartFile) {
        return MultiPartFileUtils.upload(uploadDir, multipartFile);
    }
}
