package ecsimsw.picup.service;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.StorageImageUploadRequest;
import ecsimsw.picup.dto.StorageImageUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

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
        final String albumName = request.getName();
        final String resourceKey = storageHttpClient.upload(file, albumName);
        final Album album = new Album(albumName, resourceKey);
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    @Transactional(readOnly = true)
    public AlbumInfoResponse read(Long albumId) {
        final Album album = albumRepository.findById(albumId).orElseThrow();
        return AlbumInfoResponse.of(album);
    }

    @Transactional
    public AlbumInfoResponse update(Long albumId, AlbumInfoRequest request, Optional<MultipartFile> optionalFile) {
        final Album album = albumRepository.findById(albumId).orElseThrow();
        album.updateName(request.getName());
        optionalFile.ifPresent(file -> {
            // TODO :: replace image optionalFile
            final String resourceKey = file.getName();
            album.updateThumbnailImage(resourceKey);
        });
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    @Transactional
    public void delete(Long albumId) {
        // TODO :: delete all the picture bellow
        // TODO :: soft delete
        albumRepository.deleteById(albumId);
    }

}
