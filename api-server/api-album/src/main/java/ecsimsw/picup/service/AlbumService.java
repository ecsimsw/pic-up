package ecsimsw.picup.service;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.AlbumSearchCursor;
import ecsimsw.picup.dto.ImageFileInfo;
import ecsimsw.picup.event.AlbumDeletionEvent;
import ecsimsw.picup.exception.AlbumException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static ecsimsw.picup.domain.AlbumRepository.AlbumSearchSpecs.*;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;

    public AlbumService(
        AlbumRepository albumRepository,
        FileService fileService,
        ApplicationEventPublisher eventPublisher
    ) {
        this.albumRepository = albumRepository;
        this.fileService = fileService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AlbumInfoResponse create(Long userId, AlbumInfoRequest albumInfo, MultipartFile thumbnail) {
        final String fileTag = userId.toString();
        final FileResource resource = fileService.upload(userId, thumbnail, fileTag);
        final Album album = new Album(userId, albumInfo.getName(), resource.getResourceKey());
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    @Cacheable(key = "#albumId", value = "album")
    @Transactional(readOnly = true)
    public AlbumInfoResponse read(Long userId, Long albumId) {
        final Album album = getUserAlbum(userId, albumId);
        return AlbumInfoResponse.of(album);
    }

    @CacheEvict(key = "#albumId", value = "album")
    @Transactional
    public AlbumInfoResponse update(Long userId, Long albumId, AlbumInfoRequest albumInfo, Optional<MultipartFile> optionalThumbnail) {
        final Album album = getUserAlbum(userId, albumId);
        album.updateName(albumInfo.getName());
        optionalThumbnail.ifPresent(file -> {
            final String oldImage = album.getResourceKey();
            final String fileTag = userId.toString();
            final String newImage = fileService.upload(userId, file, fileTag).getResourceKey();
            album.updateThumbnail(newImage);
            fileService.delete(oldImage);
        });
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    @CacheEvict(key = "#albumId", value = "album")
    @Transactional
    public void delete(Long userId, Long albumId) {
        final Album album = getUserAlbum(userId, albumId);

        albumRepository.delete(album);
        fileService.delete(album.getResourceKey());
        eventPublisher.publishEvent(new AlbumDeletionEvent(albumId));
    }

//    @Cacheable(key = "{#userId, #limit}", value = "userAlbumFirstPage", condition = "{#limit == 1 && #id == 2}")
    @Transactional(readOnly = true)
    public List<AlbumInfoResponse> cursorBasedFetch(Long userId, int limit, Optional<AlbumSearchCursor> cursor) {
        if(cursor.isEmpty()) {
            return fetchFirstPage(userId, limit);
        }
        final AlbumSearchCursor prev = cursor.orElseThrow();
        final List<Album> albums = albumRepository.fetch(
            where(isUser(userId))
                .and(createdLater(prev.getCreatedAt()).or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit,
            sortByCreatedAtAsc
        );
        return AlbumInfoResponse.listOf(albums);
    }

    private List<AlbumInfoResponse> fetchFirstPage(Long userId, int limit) {
        final Slice<Album> albums = albumRepository.findAllByUserId(userId, PageRequest.of(0, limit, sortByCreatedAtAsc));
        return AlbumInfoResponse.listOf(albums.getContent());
    }

    private Album getUserAlbum(Long userId, Long albumId) {
        final Album album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        album.authorize(userId);
        return album;
    }
}
