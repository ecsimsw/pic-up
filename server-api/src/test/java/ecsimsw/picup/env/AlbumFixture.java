package ecsimsw.picup.env;

import static ecsimsw.picup.env.MemberFixture.USER_ID;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.ResourceKey;

import java.util.List;

public class AlbumFixture {

    public static final Long ALBUM_ID = 1L;
    public static final String ALBUM_NAME = "album name";
    public static final ResourceKey RESOURCE_KEY = new ResourceKey("this_is_resource_key.jpg");
    public static final ResourceKey THUMBNAIL_RESOURCE_KEY = new ResourceKey("this_is_thumbnail_resource_key.jpg");

    public static final long FILE_SIZE = 256L;

    public static Album ALBUM = new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY);

    public static Picture PICTURE = new Picture(ALBUM, RESOURCE_KEY, FILE_SIZE);

    public static Picture PICTURE(Album album) {
        return new Picture(album, RESOURCE_KEY, FILE_SIZE);
    }

    public static List<ResourceKey> getResourceKeys(List<Picture> pictures) {
        return pictures.stream()
            .map(Picture::getFileResource)
            .toList();
    }
}
