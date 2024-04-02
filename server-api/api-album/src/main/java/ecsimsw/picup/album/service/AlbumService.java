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
import ecsimsw.picup.album.domain.ImageFile;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.AlbumSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.usage.service.StorageUsageService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private final PictureService pictureService;
    private final AlbumRepository albumRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public AlbumInfoResponse create(Long userId, String name, MultipartFile file) {
        var originPicture = ImageFile.of(userId, file);
        try {
            var pictureFile = fileStorageService.upload(originPicture);
            var album = new Album(userId, name, pictureFile.resourceKey(), pictureFile.size());
            albumRepository.save(album);
            return AlbumInfoResponse.of(album);
        } catch (Exception e) {
            fileStorageService.deleteAsync(originPicture.resourceKey());
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
        fileStorageService.createDeleteEvent(new FileDeletionEvent(userId, album.getResourceKey()));
        albumRepository.delete(album);
        pictureService.deleteAllInAlbum(userId, albumId);
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
