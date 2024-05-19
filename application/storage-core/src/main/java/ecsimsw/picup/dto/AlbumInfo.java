package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.ResourceKey;

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
