package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.storage.domain.ResourceKey;

import java.time.LocalDateTime;
import java.util.List;

public record AlbumInfo(
    Long id,
    String name,
    ResourceKey thumbnail,
    LocalDateTime createdAt
) {

    public static AlbumInfo of(Album album) {
        return new AlbumInfo(
            album.getId(),
            album.getName(),
            album.getThumbnail(),
            album.getCreatedAt()
        );
    }

    public static List<AlbumInfo> listOf(List<Album> albums) {
        return albums.stream()
            .map(AlbumInfo::of)
            .toList();
    }
}
