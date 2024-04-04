package ecsimsw.picup.album.service;

import static ecsimsw.picup.album.domain.AlbumRepository.AlbumSearchSpecs.ascByCreatedAt;
import static ecsimsw.picup.album.domain.AlbumRepository.AlbumSearchSpecs.createdLater;
import static ecsimsw.picup.album.domain.AlbumRepository.AlbumSearchSpecs.equalsCreatedTime;
import static ecsimsw.picup.album.domain.AlbumRepository.AlbumSearchSpecs.isUser;
import static ecsimsw.picup.album.domain.AlbumRepository.AlbumSearchSpecs.lessId;
import static ecsimsw.picup.album.domain.AlbumRepository.AlbumSearchSpecs.where;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.AlbumSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private final PictureService pictureService;
    private final AlbumRepository albumRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public AlbumInfoResponse create(Long userId, String name, ImageFileUploadResponse thumbnailFile) {
        var album = new Album(userId, name, thumbnailFile.resourceKey(), thumbnailFile.size());
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    @Transactional
    public void delete(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        fileStorageService.createDeletionEvent(new FileDeletionEvent(userId, album.getResourceKey()));
        albumRepository.delete(album);
        pictureService.deleteAllInAlbum(userId, albumId);
    }

    @Transactional(readOnly = true)
    public List<AlbumInfoResponse> cursorBasedFetch(Long userId, AlbumSearchCursor cursor) {
        if (!cursor.hasPrev()) {
            var albums = albumRepository.findAllByUserId(userId, PageRequest.of(0, cursor.limit(), ascByCreatedAt));
            return AlbumInfoResponse.listOf(albums.getContent());
        }
        var albums = albumRepository.fetch(
            where(isUser(userId))
                .and(createdLater(cursor.createdAt()).or(equalsCreatedTime(cursor.createdAt()).and(lessId(cursor.cursorId())))),
            cursor.limit(),
            ascByCreatedAt
        );
        return AlbumInfoResponse.listOf(albums);
    }

    public Album getUserAlbum(Long userId, Long albumId) {
        var album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        album.authorize(userId);
        return album;
    }
}
