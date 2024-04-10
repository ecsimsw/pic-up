package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.ecrypt.ResourceSignUrlService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceSignService {

    private final ResourceSignUrlService signUrlService;

    public ResourceSignService(ResourceSignUrlService signUrlService) {
        this.signUrlService = signUrlService;
    }

    public AlbumInfoResponse signAlbum(AlbumInfoResponse album) {
        return new AlbumInfoResponse(
            album.id(),
            album.name(),
            signUrlService.signedUrl(album.thumbnailImage()),
            album.createdAt()
        );
    }

    public List<AlbumInfoResponse> signAlbum(List<AlbumInfoResponse> albums) {
        return albums.stream()
            .map(this::signAlbum)
            .toList();
    }

    public List<PictureInfoResponse> signPictures(List<PictureInfoResponse> pictureInfos) {
        return pictureInfos.stream()
            .map(picture -> new PictureInfoResponse(
                picture.id(),
                picture.albumId(),
                picture.isVideo(),
                signUrlService.signedUrl(picture.resourceKey()),
                signUrlService.signedUrl(picture.thumbnailResourceKey()),
                picture.createdAt()
            ))
            .toList();
    }
}
