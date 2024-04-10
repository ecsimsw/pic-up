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

    public AlbumInfoResponse signAlbum(String remoteIp, AlbumInfoResponse album) {
        return new AlbumInfoResponse(
            album.id(),
            album.name(),
            signUrlService.signedUrl(remoteIp, album.thumbnailImage()),
            album.createdAt()
        );
    }

    public List<AlbumInfoResponse> signAlbums(String remoteIp, List<AlbumInfoResponse> albums) {
        return albums.stream()
            .map(album -> signAlbum(remoteIp, album))
            .toList();
    }

    public List<PictureInfoResponse> signPictures(String remoteIp, List<PictureInfoResponse> pictureInfos) {
        return pictureInfos.stream()
            .map(picture -> new PictureInfoResponse(
                picture.id(),
                picture.albumId(),
                picture.isVideo(),
                signUrlService.signedUrl(remoteIp, picture.resourceKey()),
                signUrlService.signedUrl(remoteIp, picture.thumbnailResourceKey()),
                picture.createdAt()
            ))
            .toList();
    }
}
