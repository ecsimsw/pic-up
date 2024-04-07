package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.FileReadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AlbumReadService {

    private final FileService fileService;
    private final AlbumService albumService;

    public AlbumInfoResponse album(Long userId, Long albumId) {
        var album = albumService.getUserAlbum(userId, albumId);
        return AlbumInfoResponse.of(album);
    }

    public List<AlbumInfoResponse> albums(Long userId) {
        return albumService.findAll(userId);
    }

    public FileReadResponse albumThumbnail(Long userId, Long albumId) {
        var album = albumService.getUserAlbum(userId, albumId);
        return fileService.read(album.getResourceKey());
    }
}
