package ecsimsw.picup.env;

import static ecsimsw.picup.env.MemberFixture.USER_ID;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.storage.dto.FileUploadResponse;
import ecsimsw.picup.storage.dto.VideoFileUploadResponse;
import java.time.LocalDateTime;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class AlbumFixture {

    public static final Long ALBUM_ID = 1L;
    public static final String ALBUM_NAME = "album name";
    public static final ResourceKey RESOURCE_KEY = new ResourceKey("this is resource key");
    public static final ResourceKey THUMBNAIL_RESOURCE_KEY = new ResourceKey("this is thumbnail resource key");

    public static final long FILE_SIZE = 256L;

    public static Picture PICTURE(Album album) {
        return new Picture(album, RESOURCE_KEY, THUMBNAIL_RESOURCE_KEY, FILE_SIZE);
    }

    public static Album ALBUM() {
        return new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE);
    }

    public static Album ALBUM(Long userId) {
        return new Album(userId, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE);
    }

    public static VideoFileUploadResponse VIDEO_FILE = new VideoFileUploadResponse(RESOURCE_KEY, THUMBNAIL_RESOURCE_KEY, 1L);

    public static PictureInfoResponse PICTURE_INFO_RESPONSE = new PictureInfoResponse(USER_ID, ALBUM_ID, false, RESOURCE_KEY.getResourceKey(), THUMBNAIL_RESOURCE_KEY.getResourceKey(), LocalDateTime.now());

    public static FileUploadResponse IMAGE_FILE = new FileUploadResponse(RESOURCE_KEY, FILE_SIZE);
    public static FileUploadResponse THUMBNAIL_FILE = new FileUploadResponse(RESOURCE_KEY, FILE_SIZE);

    public static final MultipartFile MULTIPART_FILE = new MockMultipartFile(RESOURCE_KEY.getResourceKey(), RESOURCE_KEY.getResourceKey(), "png", "Image binary file for test".getBytes());

    public static MultipartFile MULTIPART_FILE(String fileName) {
        return new MockMultipartFile(fileName, fileName, "png", "Image binary file for test".getBytes());
    }
}
