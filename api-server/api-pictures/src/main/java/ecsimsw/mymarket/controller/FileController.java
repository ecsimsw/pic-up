package ecsimsw.mymarket.controller;

import ecsimsw.mymarket.dataUtils.FileUploadResult;
import ecsimsw.mymarket.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileController {

  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @PostMapping("/api/picture/upload")
  public ResponseEntity<FileUploadResult> upload(MultipartFile file) {
    final FileUploadResult fileUploadResult = fileService.fileUpload(file);
    return ResponseEntity.ok(fileUploadResult);
  }
}
