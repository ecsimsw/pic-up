package ecsimsw.mymarket.dataUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class MultiPartFileUtils {

  public static FileUploadResult upload(String rootDirectory, MultipartFile file) {
    if(file == null) {
      throw new IllegalArgumentException("File is empty");
    }
    try {
      final Path path = Paths.get(rootDirectory + File.separator + StringUtils.cleanPath(file.getOriginalFilename()));
      final long uploadedBytes = Files.copy(file.getInputStream(), path);
      return new FileUploadResult(uploadedBytes, path.toString());
    } catch (FileAlreadyExistsException e) {
      e.printStackTrace();
      System.out.println("File already exists");
      throw new IllegalArgumentException("File already existse : " + file.getOriginalFilename());
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not store file : " + file.getOriginalFilename());
    }
  }
}
