package ecsimsw.picup.env;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.storage.StorageKey;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class FileFixture {

    public static String FILE_TAG = "TAG";

    public static Long USER_ID = 1L;

    public static MultipartFile MULTIPART_FILE = new MockMultipartFile("name", "name.png", "png", "Image binary file for test".getBytes());

    public static ImageFile IMAGE_FILE = ImageFile.of(MULTIPART_FILE);

    public static String RESOURCE_KEY = "1-26169a90-e4a6-4c0f-9550-67397a5105d7.jpg";
}
