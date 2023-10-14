package ecsimsw.picup.service;

import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.dto.PictureInfoRequest;
import ecsimsw.picup.dto.PictureInfoResponse;
import ecsimsw.picup.dto.UpdatePictureOrderRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PictureService {

    private final PictureRepository pictureRepository;
    private final StorageHttpClient storageHttpClient;

    public PictureService(PictureRepository pictureRepository, StorageHttpClient storageHttpClient) {
        this.pictureRepository = pictureRepository;
        this.storageHttpClient = storageHttpClient;
    }

    @Transactional
    public PictureInfoResponse create(Long albumId, PictureInfoRequest request, MultipartFile file) {
        final String username = "hi";
        final String resourceKey = storageHttpClient.upload(file, username);
        final Picture picture = new Picture(albumId, resourceKey, request.getDescription(), lastOrderInAlbum(albumId) + 1);
        pictureRepository.save(picture);
        return PictureInfoResponse.of(picture);
    }

    @Transactional(readOnly = true)
    public PictureInfoResponse read(Long pictureId) {
        final Picture picture = pictureRepository.findById(pictureId).orElseThrow();
        return PictureInfoResponse.of(picture);
    }

    @Transactional(readOnly = true)
    public List<PictureInfoResponse> listAll(Long albumId) {
        final List<Picture> pictures = pictureRepository.findAllByAlbumId(albumId);
        return PictureInfoResponse.listOf(pictures);
    }

    @Transactional
    public void delete(Long albumId, Long pictureId) {
        final Picture picture = pictureRepository.findById(pictureId).orElseThrow();
        picture.validateAlbum(albumId);
        storageHttpClient.delete(picture.getResourceKey());
        pictureRepository.delete(picture);
    }

    @Transactional
    public PictureInfoResponse update(Long albumId, Long pictureId, PictureInfoRequest request, Optional<MultipartFile> optionalFile) {
        final String username = "username";
        final Picture picture = pictureRepository.findById(pictureId).orElseThrow();
        picture.validateAlbum(albumId);
        picture.updateDescription(request.getDescription());

        optionalFile.ifPresent(file -> {
            final String oldImage = picture.getResourceKey();
            final String newImage = storageHttpClient.upload(file, username);
            picture.updateImage(newImage);
            storageHttpClient.delete(oldImage);
        });

        pictureRepository.save(picture);
        return PictureInfoResponse.of(picture);
    }

    @Transactional
    public List<PictureInfoResponse> updateOrder(Long albumId, List<UpdatePictureOrderRequest> orderInfos) {
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

    private int lastOrderInAlbum(Long albumId) {
        return pictureRepository.findTopByAlbumIdOrderByOrderNumber(albumId)
            .map(Picture::getOrderNumber)
            .orElse(0);
    }
}
