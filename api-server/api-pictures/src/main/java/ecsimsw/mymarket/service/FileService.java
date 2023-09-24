package ecsimsw.mymarket.service;

import ecsimsw.mymarket.dataUtils.FileUploadResult;
import ecsimsw.mymarket.dataUtils.MultiPartFileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

  private final String uploadDir;

  public FileService(
      @Value("${file.root.directory:./}") String uploadDir
  ) {
    this.uploadDir = uploadDir;
  }

  public FileUploadResult fileUpload(MultipartFile multipartFile) {
    return MultiPartFileUtils.upload(uploadDir, multipartFile);
  }
}
