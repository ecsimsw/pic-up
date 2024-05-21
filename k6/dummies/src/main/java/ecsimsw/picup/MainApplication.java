package ecsimsw.picup;

import ecsimsw.picup.utils.RandomUtils;

import java.time.LocalDateTime;
import java.util.List;

import static ecsimsw.picup.utils.FileUtils.generate;

public class MainApplication {

    private static final int NUMBER_OF_MEMBER = 100;
    private static final int NUMBER_OF_STORAGE_USAGE_PER_MEMBER = 1;
    private static final int NUMBER_OF_ALBUM_PER_MEMBER = 10;
    private static final int NUMBER_OF_PICTURE_PRE_ALBUM = 10000;

    public static void main(String[] args) {
        var memberId = 1;
        var storageUsageId = 1;
        var albumId = 1;
        var pictureId = 1;
        var fileResourceId = 1;

        // generate member
        var member = member();
        generate(memberId, member, NUMBER_OF_MEMBER);

        // init member's storage usage
        for (long u = 1; u <= NUMBER_OF_MEMBER; u++) {
            var storageUsage = storageUsage(u);
            generate(storageUsageId, storageUsage, NUMBER_OF_STORAGE_USAGE_PER_MEMBER);
        }

        // generate album
        for (long u = 1; u <= NUMBER_OF_MEMBER; u++) {
            var fileResource = fileResource();
            fileResourceId += generate(fileResourceId, fileResource, NUMBER_OF_ALBUM_PER_MEMBER);

            var album = album(u);
            albumId += generate(albumId, album, NUMBER_OF_ALBUM_PER_MEMBER);

            // generate picture in album
            var albumIdFrom = albumId - NUMBER_OF_ALBUM_PER_MEMBER;
            var albumIdTo = albumId;
            for (int p = 0; p < NUMBER_OF_PICTURE_PRE_ALBUM; p++) {
                var picture = picture(albumIdFrom, albumIdTo);
                pictureId += generate(pictureId, picture, NUMBER_OF_PICTURE_PRE_ALBUM);
            }
        }
    }

    private static DataCsvFile member() {
        return new DataCsvFile(
            "member-data.txt",
            List.of("id", "encrypted", "salt", "username"),
            id -> List.of(
                id,
                "13601bda4ea78e55a07b98866d2be6be0744e3866f13c00c811cab608a28f322",
                "salt",
                "user-" + id
            )
        );
    }

    private static DataCsvFile storageUsage(Long userId) {
        return new DataCsvFile(
            "storage-usage-data.txt",
            List.of("user_id", "limit_as_byte", "usage_as_byte"),
            id -> List.of(
                userId,
                Long.MAX_VALUE,
                0
            )
        );
    }

    private static DataCsvFile picture(int albumIdMin, int albumIdMax) {
        return new DataCsvFile(
            "picture-data.txt",
            List.of("id", "created_at", "resource_key", "size", "has_thumbnail", "album_id"),
            id -> List.of(
                id,
                LocalDateTime.now(),
                "hi.jpg",
                1,
                false,
                RandomUtils.number(albumIdMin, albumIdMax)
            )
        );
    }

    private static DataCsvFile album(long userId) {
        return new DataCsvFile(
            "album-data.txt",
            List.of("id", "created_at", "name", "resource_key", "user_id"),
            id -> List.of(
                id,
                LocalDateTime.now(),
                "album-name",
                "hi.jpg",
                userId
            )
        );
    }

    private static DataCsvFile fileResource() {
        return new DataCsvFile(
            "file-resource-data.txt",
            List.of("id", "created_at", "size", "resource_key", "storage_type", "to_be_deleted"),
            id -> List.of(
                id,
                LocalDateTime.now(),
                1,
                "hi.jpg",
                "STORAGE",
                false
            )
        );
    }
}
