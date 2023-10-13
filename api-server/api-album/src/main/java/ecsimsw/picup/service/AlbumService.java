package ecsimsw.picup.service;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
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
        System.out.println(resourceKey);
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

}
