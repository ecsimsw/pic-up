package ecsimsw.picup.env;

import static ecsimsw.picup.env.MemberFixture.MEMBER_ID;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.dto.ImageFileUploadResponse;
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
        return new Album(MEMBER_ID, ALBUM_NAME, RESOURCE_KEY, SIZE);
    }

    public static Album ALBUM(Long userId) {
        return new Album(userId, ALBUM_NAME, RESOURCE_KEY, SIZE);
    }

    public static ImageFileUploadResponse IMAGE_FILE = new ImageFileUploadResponse("name.png", 1L);

}
