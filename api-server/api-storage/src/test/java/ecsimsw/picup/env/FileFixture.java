package ecsimsw.picup.env;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.StorageKey;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class FileFixture {

    public static String FILE_TAG = "TAG";

    public static MultipartFile MULTIPART_FILE = new MockMultipartFile("name", "name.png", "png", "Image binary file for test".getBytes());

    public static ImageFile IMAGE_FILE = ImageFile.of(MULTIPART_FILE);

    public static Resource createdResource(String key) {
        return new Resource(
            key,
            List.of(StorageKey.LOCAL_FILE_STORAGE, StorageKey.S3_OBJECT_STORAGE),
            LocalDateTime.now(),
            null
        );
    }
}
