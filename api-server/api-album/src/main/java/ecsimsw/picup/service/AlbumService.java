package ecsimsw.picup.service;

import static ecsimsw.picup.domain.AlbumRepository.AlbumSearchSpecs.createdLater;
import static ecsimsw.picup.domain.AlbumRepository.AlbumSearchSpecs.where;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.Album_;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.event.AlbumDeletionEvent;
import java.util.List;
import java.util.Optional;

import ecsimsw.picup.exception.AlbumException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public AlbumInfoResponse create(AlbumInfoRequest albumInfo, MultipartFile thumbnail) {
        final Long userId = 1L;
        final String resourceKey = fileService.upload(thumbnail, userId.toString());
        final Album album = new Album(userId, albumInfo.getName(), resourceKey);
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    @Transactional(readOnly = true)
    public AlbumInfoResponse read(Long albumId) {
        final Album album = albumRepository.findById(albumId).orElseThrow(()-> new AlbumException("Invalid album"));
        return AlbumInfoResponse.of(album);
    }

    @Transactional
    public AlbumInfoResponse update(Long albumId, AlbumInfoRequest albumInfo, Optional<MultipartFile> optionalThumbnail) {
        final Long userId = 1L;
        final Album album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        album.updateName(albumInfo.getName());
        optionalThumbnail.ifPresent(file -> {
            final String oldImage = album.getResourceKey();
            final String newImage = fileService.upload(file, userId.toString());
            album.updateThumbnail(newImage);
            fileService.delete(oldImage);
        });
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    @Transactional
    public void delete(Long albumId) {
        final Album album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        albumRepository.delete(album);
        fileService.delete(album.getResourceKey());
        eventPublisher.publishEvent(new AlbumDeletionEvent(albumId));
    }

    @Transactional(readOnly = true)
    public List<AlbumInfoResponse> cursorByOrder(int limit, int prev) {
        final List<Album> albums = albumRepository.fetch(
            where(createdLater(prev)),
            limit,
            Sort.by(Direction.ASC, Album_.CREATED_AT)
        );
        return AlbumInfoResponse.listOf(albums);
    }
}
