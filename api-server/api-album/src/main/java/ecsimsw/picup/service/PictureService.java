package ecsimsw.picup.service;

import static ecsimsw.picup.domain.PictureRepository.PictureSearchSpecs.greaterOrderThan;
import static ecsimsw.picup.domain.PictureRepository.PictureSearchSpecs.isAlbum;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.domain.PictureRepository.PictureSearchSpecs;
import ecsimsw.picup.domain.Picture_;
import ecsimsw.picup.dto.PictureInfoRequest;
import ecsimsw.picup.dto.PictureInfoResponse;
import ecsimsw.picup.dto.UpdatePictureOrderRequest;
import ecsimsw.picup.event.AlbumDeletionEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public PictureInfoResponse create(Long albumId, PictureInfoRequest pictureInfo, MultipartFile imageFile) {
        final Long userId = 1L;
        final Album album = albumRepository.findById(albumId).orElseThrow();
        final String resourceKey = fileService.upload(imageFile, userId.toString());
        final Picture picture = new Picture(albumId, resourceKey, pictureInfo.getDescription(), lastOrderNumber(albumId) + 1);
        pictureRepository.save(picture);
        return PictureInfoResponse.of(picture);
    }

    @Transactional(readOnly = true)
    public PictureInfoResponse read(Long pictureId) {
        final Picture picture = pictureRepository.findById(pictureId).orElseThrow();
        return PictureInfoResponse.of(picture);
    }

    @Transactional(readOnly = true)
    public List<PictureInfoResponse> cursorByOrder(Long albumId, int limit, int prev) {
        var searchSpec = PictureSearchSpecs.where()
            .and(isAlbum(albumId))
            .and(greaterOrderThan(prev));
        var pageable = PageRequest.of(0, limit, Sort.by(Direction.ASC, Picture_.ORDER_NUMBER));
        final List<Picture> pictures = pictureRepository.fetch(searchSpec, pageable);
        return PictureInfoResponse.listOf(pictures);
    }

    @Transactional
    public void delete(Long albumId, Long pictureId) {
        final Album album = albumRepository.findById(albumId).orElseThrow();
        final Picture picture = pictureRepository.findById(pictureId).orElseThrow();
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
    public PictureInfoResponse update(Long albumId, Long pictureId, PictureInfoRequest pictureInfo, Optional<MultipartFile> optionalImageFile) {
        final Long userId = 1L;
        final Album album = albumRepository.findById(albumId).orElseThrow();
        final Picture picture = pictureRepository.findById(pictureId).orElseThrow();
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

    @Transactional
    public List<PictureInfoResponse> updateOrder(Long albumId, List<UpdatePictureOrderRequest> orderInfos) {
        final Album album = albumRepository.findById(albumId).orElseThrow();
        final Set<Integer> usedOrder = new HashSet<>();
        pictureRepository.findAllByAlbumId(albumId).forEach(picture -> {
            orderInfos.stream()
                .filter(it -> it.isPicture(picture))
                .forEach(it -> picture.updateOrder(it.getOrder()));
            if (usedOrder.contains(picture.getOrderNumber())) {
                throw new IllegalArgumentException("Order can't be duplicated");
            }
            usedOrder.add(picture.getOrderNumber());
        });
        final List<Picture> pictures = pictureRepository.findAllByAlbumId(albumId);
        return PictureInfoResponse.listOf(pictures);
    }

    private int lastOrderNumber(Long albumId) {
        final PageRequest requestTop1 = PageRequest.of(0, 1, Sort.by(Direction.DESC, Picture_.ORDER_NUMBER, Picture_.ID));
        return pictureRepository.findAllByAlbumId(albumId, requestTop1).stream()
            .map(Picture::getOrderNumber)
            .findFirst()
            .orElse(0);
    }
}
