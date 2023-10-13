package ecsimsw.picup.service;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.UpdateAlbumOrderRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final StorageHttpClient storageHttpClient;

    public AlbumService(AlbumRepository albumRepository, StorageHttpClient storageHttpClient) {
        this.albumRepository = albumRepository;
        this.storageHttpClient = storageHttpClient;
    }

    @Transactional
    public AlbumInfoResponse create(AlbumInfoRequest request, MultipartFile file) {
        final String name = request.getName();
        final String resourceKey = storageHttpClient.upload(file, name);
        final Album album = new Album(name, resourceKey, lastOrder() + 1);
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    @Transactional(readOnly = true)
    public AlbumInfoResponse read(Long albumId) {
        final Album album = albumRepository.findById(albumId).orElseThrow();
        return AlbumInfoResponse.of(album);
    }

    @Transactional
    public AlbumInfoResponse update(Long albumId, AlbumInfoRequest request,
        Optional<MultipartFile> optionalFile) {
        final Album album = albumRepository.findById(albumId).orElseThrow();
        album.updateName(request.getName());
        optionalFile.ifPresent(file -> {
            storageHttpClient.delete(album.getThumbnailResourceKey());
            final String resourceKey = storageHttpClient.upload(file, album.getName());
            album.updateThumbnail(resourceKey);
        });
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    // TODO :: delete all the picture bellow
    // TODO :: soft delete

    @Transactional
    public void delete(Long albumId) {
        final Album album = albumRepository.findById(albumId).orElseThrow();
        albumRepository.delete(album);
        storageHttpClient.delete(album.getThumbnailResourceKey());
    }

    @Transactional(readOnly = true)
    public List<AlbumInfoResponse> listAll() {
        final List<Album> albums = albumRepository.findAll();
        return AlbumInfoResponse.listOf(albums);
    }

    @Transactional
    public List<AlbumInfoResponse> updateOrder(List<UpdateAlbumOrderRequest> orderInfos) {
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
        final List<Album> albums = albumRepository.findAllByOrderByOrderNumber();
        return AlbumInfoResponse.listOf(albums);
    }

    private Integer lastOrder() {
        return albumRepository.findTopByOrder()
            .map(Album::getOrderNumber)
            .orElse(0);
    }
}
