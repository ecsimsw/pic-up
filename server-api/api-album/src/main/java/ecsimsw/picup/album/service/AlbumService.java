package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.AlbumSearchCursor;
import ecsimsw.picup.album.dto.FileResourceInfo;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.usage.service.StorageUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static ecsimsw.picup.album.domain.AlbumRepository.AlbumSearchSpecs.*;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileService fileService;
    private final StorageUsageService storageUsageService;

    @Transactional
    public AlbumInfoResponse create(Long userId, String name, FileResourceInfo resource) {
        try {
            var album = new Album(userId, name, resource.resourceKey(), resource.size());
            storageUsageService.addUsage(userId, resource.size());
            albumRepository.save(album);
            return AlbumInfoResponse.of(album);
        } catch (Exception e) {
            fileService.delete(resource.resourceKey());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public AlbumInfoResponse read(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        return AlbumInfoResponse.of(album);
    }

    @Transactional
    public void delete(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        fileService.createDeleteEvent(new FileDeletionEvent(userId, album.getThumbnailResourceKey()));
        storageUsageService.subtractUsage(userId, album.getThumbnailFileSize());
        albumRepository.delete(album);

        var pictures = pictureRepository.findAllByAlbumId(albumId);
        fileService.createDeleteEvents(FileDeletionEvent.listOf(userId, pictures));
        storageUsageService.subtractUsage(userId, pictures.stream().mapToLong(Picture::getFileSize).sum());
        pictureRepository.deleteAll(pictures);
    }

    @Transactional(readOnly = true)
    public List<AlbumInfoResponse> cursorBasedFetch(Long userId, int limit, Optional<AlbumSearchCursor> cursor) {
        if (cursor.isEmpty()) {
            var albums = albumRepository.findAllByUserId(userId, PageRequest.of(0, limit, ascByCreatedAt));
            return AlbumInfoResponse.listOf(albums.getContent());
        }
        var prev = cursor.orElseThrow();
        var albums = albumRepository.fetch(
            where(isUser(userId))
                .and(createdLater(prev.createdAt()).or(equalsCreatedTime(prev.createdAt()).and(lessId(prev.id())))),
            limit,
            ascByCreatedAt
        );
        return AlbumInfoResponse.listOf(albums);
    }

    private Album getUserAlbum(Long userId, Long albumId) {
        var album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        album.authorize(userId);
        return album;
    }
}
