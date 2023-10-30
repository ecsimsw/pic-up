package ecsimsw.picup.utils;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.StorageKey;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class FileFixture {

    public static String fakeTag = "TAG";

    public static MultipartFile mockMultipartFile = new MockMultipartFile("name", "name.png", "png", "Image binary file for test".getBytes());

    public static ImageFile fakeImageFile = ImageFile.of(mockMultipartFile);

    public static Resource createdResource(String key) {
        return new Resource(
            key,
            List.of(StorageKey.MAIN_STORAGE, StorageKey.BACKUP_STORAGE),
            LocalDateTime.now(),
            null
        );
    }
}
