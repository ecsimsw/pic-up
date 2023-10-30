package ecsimsw.picup.utils;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class FileFixture {

    public static String mockTag = "TAG";
    public static MultipartFile mockFile = new MockMultipartFile("name", "name.png", "png", "Image binary file for test".getBytes());
}
