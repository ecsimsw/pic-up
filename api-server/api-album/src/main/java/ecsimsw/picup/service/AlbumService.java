package ecsimsw.picup.service;

import static ecsimsw.picup.domain.AlbumRepository.AlbumSearchSpecs.greaterOrder;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.AlbumRepository.AlbumSearchSpecs;
import ecsimsw.picup.domain.Album_;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.UpdateAlbumOrderRequest;
import ecsimsw.picup.event.AlbumDeletionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
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
        final Album album = new Album(userId, albumInfo.getName(), resourceKey, lastOrder(userId) + 1);
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    @Transactional(readOnly = true)
    public AlbumInfoResponse read(Long albumId) {
        final Album album = albumRepository.findById(albumId).orElseThrow();
        return AlbumInfoResponse.of(album);
    }

    @Transactional
    public AlbumInfoResponse update(Long albumId, AlbumInfoRequest albumInfo, Optional<MultipartFile> optionalThumbnail) {
        final Long userId = 1L;
        final Album album = albumRepository.findById(albumId).orElseThrow();
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
        final Album album = albumRepository.findById(albumId).orElseThrow();
        albumRepository.delete(album);
        fileService.delete(album.getResourceKey());
        eventPublisher.publishEvent(new AlbumDeletionEvent(albumId));
    }

    @Transactional(readOnly = true)
    public List<AlbumInfoResponse> cursorByOrder(int limit, int prev) {
        var searchSpec = AlbumSearchSpecs.where()
            .and(greaterOrder(prev));
        var pageable = PageRequest.of(0, limit, Sort.by(Direction.ASC, Album_.ORDER_NUMBER));
        final List<Album> albums = albumRepository.fetch(searchSpec, pageable);
        return AlbumInfoResponse.listOf(albums);
    }

    @Transactional
    public void updateOrder(List<UpdateAlbumOrderRequest> orderInfos) {
        final Set<Integer> usedOrder = new HashSet<>();
        albumRepository.findAll().forEach(album -> {
            orderInfos.stream()
                .filter(it -> it.isAlbum(album))
                .forEach(it -> album.updateOrder(it.getOrder()));
            if (usedOrder.contains(album.getOrderNumber())) {
                throw new IllegalArgumentException("Order can't be duplicated");
            }
            usedOrder.add(album.getOrderNumber());
        });
    }

    private Integer lastOrder(Long userId) {
        final PageRequest requestTop1 = PageRequest.of(0, 1, Sort.by(Direction.DESC, Album_.ORDER_NUMBER, Album_.ID));
        return albumRepository.findAllByUserId(userId, requestTop1).stream()
            .map(Album::getOrderNumber)
            .findFirst()
            .orElse(0);
    }
}
