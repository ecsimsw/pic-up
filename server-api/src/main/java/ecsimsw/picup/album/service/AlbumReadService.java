package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AlbumReadService {

    private final AlbumService albumService;

    public AlbumInfoResponse album(Long userId, Long albumId) {
        var album = albumService.getUserAlbum(userId, albumId);
        System.out.println(album.getResourceKey().value());
        AlbumInfoResponse albumInfoResponse = AlbumInfoResponse.of(album);
        System.out.println(albumInfoResponse.thumbnailUrl());
        return albumInfoResponse;
    }

    public List<AlbumInfoResponse> albums(Long userId) {
        var albums = albumService.findAll(userId);
        return AlbumInfoResponse.listOf(albums);
    }
}
