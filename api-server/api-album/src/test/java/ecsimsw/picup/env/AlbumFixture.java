package ecsimsw.picup.env;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;

import static ecsimsw.picup.auth.config.AuthTokenWebConfig.ACCESS_TOKEN_COOKIE_KEY;
import static ecsimsw.picup.auth.config.AuthTokenWebConfig.REFRESH_TOKEN_COOKIE_KEY;

public class AlbumFixture {

    public static final Long ALBUM_ID = 1L;
    public static final String ALBUM_NAME = "album name";
    public static final String THUMBNAIL_RESOURCE_KEY = "this is thumbnail resource key";
    public static final String RESOURCE_KEY = "this is resource key";
    public static final long SIZE = 256L;
    public static final String DESCRIPTION = "this is description of picture";

    public static final MultipartFile MULTIPART_FILE = new MockMultipartFile("name", "name.png", "png", "Image binary file for test".getBytes());
    public static final MultipartFile INVALID_MULTIPART_FILE = new MockMultipartFile("name", "name", "png", "Image binary file for test".getBytes());

    public static final String TAG = "tag";
}
