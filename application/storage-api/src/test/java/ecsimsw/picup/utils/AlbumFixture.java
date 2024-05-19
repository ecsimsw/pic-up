package ecsimsw.picup.utils;

import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.domain.ResourceKey;

import static ecsimsw.picup.domain.StorageType.THUMBNAIL;

public class AlbumFixture {

    public static final Long ALBUM_ID = 1L;
    public static final String ALBUM_NAME = "album name";

    public static final ResourceKey RESOURCE_KEY = new ResourceKey("this_is_resource_key.jpg");
    public static final ResourceKey THUMBNAIL_RESOURCE_KEY = new ResourceKey("this_is_thumbnail_resource_key.jpg");

    public static final String FILE_NAME = "this_is_resource_key.jpg";
    public static final long FILE_SIZE = 256L;

    public static final FileResource THUMBNAIL_FILE = new FileResource(THUMBNAIL, RESOURCE_KEY, FILE_SIZE, false);

    public static final Long USER_ID = 1L;
    public static final String USER_NAME = "USER_NAME";
}
