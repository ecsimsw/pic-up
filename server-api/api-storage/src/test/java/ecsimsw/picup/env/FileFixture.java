package ecsimsw.picup.env;

import ecsimsw.picup.domain.StoredFile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class FileFixture {

    public static String FILE_TAG = "TAG";

    public static Long USER_ID = 1L;

    public static MultipartFile MULTIPART_FILE = new MockMultipartFile("name", "name.png", "png",
        "Image binary file for test".getBytes());

    public static StoredFile IMAGE_FILE = StoredFile.of(MULTIPART_FILE);

    public static String RESOURCE_KEY = "1-26169a90-e4a6-4c0f-9550-67397a5105d7.jpg";
}
