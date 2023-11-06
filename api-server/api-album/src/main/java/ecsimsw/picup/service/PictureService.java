package ecsimsw.picup.service;

import ecsimsw.picup.domain.*;
import ecsimsw.picup.dto.PictureInfoRequest;
import ecsimsw.picup.dto.PictureInfoResponse;
import ecsimsw.picup.dto.PictureSearchCursor;
import ecsimsw.picup.event.AlbumDeletionEvent;
import ecsimsw.picup.exception.AlbumException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ecsimsw.picup.domain.PictureRepository.PictureSearchSpecs.*;

@Service
public class PictureService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileService fileService;

    public PictureService(
        AlbumRepository albumRepository,
        PictureRepository pictureRepository,
        FileService fileService
    ) {
        this.albumRepository = albumRepository;
        this.pictureRepository = pictureRepository;
        this.fileService = fileService;
    }

    @Transactional
    public PictureInfoResponse create(Long userId, Long albumId, PictureInfoRequest pictureInfo, MultipartFile imageFile) {
        final Album album = albumRepository.findById(albumId).orElseThrow();
        final String resourceKey = fileService.upload(imageFile, userId.toString());
        final Picture picture = new Picture(albumId, resourceKey, pictureInfo.getDescription());
        pictureRepository.save(picture);
        return PictureInfoResponse.of(picture);
    }

    @Transactional(readOnly = true)
    public PictureInfoResponse read(Long userId, Long pictureId) {
        final Picture picture = pictureRepository.findById(pictureId).orElseThrow();
        return PictureInfoResponse.of(picture);
    }

    @Transactional(readOnly = true)
    public List<PictureInfoResponse> cursorBasedFetch(Long UserId, Long albumId, int limit, Optional<PictureSearchCursor> cursor) {
        if(cursor.isEmpty()) {
            return fetchFirstPage(albumId, limit, sortByCreatedAtAsc);
        }
        final PictureSearchCursor prev = cursor.orElseThrow();
        final List<Picture> pictures = pictureRepository.fetch(
            where(isAlbum(albumId))
                .and(createdLater(prev.getCreatedAt()).or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit,
            sortByCreatedAtAsc
        );
        return PictureInfoResponse.listOf(pictures);
    }

    private List<PictureInfoResponse> fetchFirstPage(Long albumId, int limit, Sort sortByCreatedAtAsc) {
        final Slice<Picture> pictures = pictureRepository.findAllByAlbumId(albumId, PageRequest.of(0, limit, sortByCreatedAtAsc));
        return PictureInfoResponse.listOf(pictures.getContent());
    }

    @Transactional
    public void delete(Long userId, Long albumId, Long pictureId) {
        final Album album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        final Picture picture = pictureRepository.findById(pictureId).orElseThrow(() -> new AlbumException("Invalid picture"));
        picture.validateAlbum(albumId);
        fileService.delete(picture.getResourceKey());
        pictureRepository.delete(picture);
    }

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteAllInAlbum(AlbumDeletionEvent event) {
        final List<Picture> pictures = pictureRepository.findAllByAlbumId(event.getAlbumId());
        final List<String> imagesToDelete = pictures.stream()
            .map(Picture::getResourceKey)
            .collect(Collectors.toList());
        fileService.deleteAll(imagesToDelete);
        pictureRepository.deleteAll(pictures);
    }

    @Transactional
    public PictureInfoResponse update(Long userId, Long albumId, Long pictureId, PictureInfoRequest pictureInfo, Optional<MultipartFile> optionalImageFile) {
        final Album album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        final Picture picture = pictureRepository.findById(pictureId).orElseThrow(() -> new AlbumException("Invalid picture"));
        picture.validateAlbum(albumId);
        picture.updateDescription(pictureInfo.getDescription());
        optionalImageFile.ifPresent(file -> {
            final String oldImage = picture.getResourceKey();
            final String newImage = fileService.upload(file, userId.toString());
            picture.updateImage(newImage);
            fileService.delete(oldImage);
        });
        pictureRepository.save(picture);
        return PictureInfoResponse.of(picture);
    }
}
