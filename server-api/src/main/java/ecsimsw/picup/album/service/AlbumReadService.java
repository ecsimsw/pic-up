package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AlbumReadService {

    private final ResourceSignService resourceSignService;
    private final AlbumService albumService;

    public AlbumInfoResponse album(Long userId, Long albumId) {
        var album = albumService.getUserAlbum(userId, albumId);
        return resourceSignService.signAlbums(album);
    }

    public List<AlbumInfoResponse> albums(Long userId) {
        var albums = albumService.findAll(userId);
        return resourceSignService.signAlbum(albums);
    }
}
