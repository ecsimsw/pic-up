package ecsimsw.picup.env;

import static ecsimsw.picup.env.MemberFixture.USER_ID;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.storage.dto.FileUploadResponse;
import ecsimsw.picup.storage.dto.VideoFileUploadResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class AlbumFixture {

    public static final Long ALBUM_ID = 1L;
    public static final String ALBUM_NAME = "album name";
    public static final String RESOURCE_KEY = "this is resource key";
    public static final String THUMBNAIL_RESOURCE_KEY = "this is thumbnail resource key";
    public static final long SIZE = 256L;

    public static final List<String> RESOURCES = List.of(RESOURCE_KEY, RESOURCE_KEY, RESOURCE_KEY);

    public static Picture PICTURE(Album album) {
        return new Picture(album, RESOURCE_KEY, THUMBNAIL_RESOURCE_KEY, SIZE);
    }

    public static Album ALBUM() {
        return new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, SIZE);
    }

    public static Album ALBUM(Long userId) {
        return new Album(userId, ALBUM_NAME, RESOURCE_KEY, SIZE);
    }

    public static VideoFileUploadResponse VIDEO_FILE = new VideoFileUploadResponse("name.png", "thumbnail.png", 1L);

    public static FileUploadResponse IMAGE_FILE = new FileUploadResponse("name.png", 1L);

    public static FileUploadResponse THUMBNAIL_FILE = new FileUploadResponse("name.png", 1L);

    public static final MultipartFile MULTIPART_FILE = new MockMultipartFile("name", "name.png", "png", "Image binary file for test".getBytes());

    public static MultipartFile MULTIPART_FILE(String fileName) {
        return new MockMultipartFile(fileName, fileName, "png", "Image binary file for test".getBytes());
    }
}
