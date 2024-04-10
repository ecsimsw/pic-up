package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;

import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ResourceSignService {

    private static final String CDN_PROTOCOL = "https";
    private static final int EXPIRATION_AFTER_DAYS = 7;

    private final CloudFrontUtilities cloudFrontUtilities;
    private final String domainName;
    private final String publicKeyId;
    private final String privateKeyPath;

    public ResourceSignService(
        String domainName,
        String publicKeyId,
        String privateKeyPath
    ) {
        this.domainName = domainName;
        this.publicKeyId = publicKeyId;
        this.privateKeyPath = privateKeyPath;
        this.cloudFrontUtilities = CloudFrontUtilities.create();
    }

    public AlbumInfoResponse signAlbum(AlbumInfoResponse album) {
        return new AlbumInfoResponse(
            album.id(),
            album.name(),
            signedUrl(album.thumbnailImage()),
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
                signedUrl(picture.resourceKey()),
                signedUrl(picture.thumbnailResourceKey()),
                picture.createdAt()
            ))
            .toList();
    }

    public String signedUrl(String fileName) {
        try {
            var sign = cannedSign(fileName);
            var signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(sign);
            return signedUrl.url();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create cloudfront sign url from : " + fileName);
        }
    }

    private CannedSignerRequest cannedSign(String fileName) throws Exception {
        return CannedSignerRequest.builder()
            .privateKey(Path.of(privateKeyPath))
            .resourceUrl(new URL(CDN_PROTOCOL, domainName, "/" + fileName).toString())
            .keyPairId(publicKeyId)
            .expirationDate(Instant.now().plus(EXPIRATION_AFTER_DAYS, ChronoUnit.DAYS))
            .build();
    }
}
