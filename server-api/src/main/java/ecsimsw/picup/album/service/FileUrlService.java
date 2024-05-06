package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.FileResource;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.domain.StorageType;
import ecsimsw.picup.album.dto.PreUploadUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;

import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static ecsimsw.picup.album.domain.CacheType.SIGNED_URL;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileUrlService {

    private static final String CDN_PROTOCOL = "https";
    private static final int SIGNED_URL_EXPIRATION_AFTER_DAYS = 7;

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();

    @Value("${aws.cloudfront.domain}")
    private String domainName;

    @Value("${aws.cloudfront.publicKeyId}")
    private String publicKeyId;

    @Value("${aws.cloudfront.privateKeyPath}")
    private String privateKeyPath;

    private final FileResourceService fileResourceService;
    private final FileStorageService fileStorageService;

    @Cacheable(value = SIGNED_URL, key = "{#storageType, #remoteIp, #fileResource.value()}")
    public String fileUrl(StorageType storageType, String remoteIp, ResourceKey fileResource) {
        var filePath = fileResourceService.filePath(storageType, fileResource);
        try {
            var sign = cannedSign(remoteIp, filePath);
            var signedUrl = cloudFrontUtilities.getSignedUrlWithCustomPolicy(sign);
            return signedUrl.url();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create cloudfront sign url from : " + filePath);
        }
    }

    private CustomSignerRequest cannedSign(String remoteIp, String resourcePath) throws Exception {
        return CustomSignerRequest.builder()
            .privateKey(Path.of(privateKeyPath))
            .ipRange(remoteIp + "/32")
            .resourceUrl(new URL(CDN_PROTOCOL, domainName, "/" + resourcePath).toString())
            .keyPairId(publicKeyId)
            .expirationDate(Instant.now().plus(SIGNED_URL_EXPIRATION_AFTER_DAYS, ChronoUnit.DAYS))
            .build();
    }

    public PreUploadUrlResponse uploadUrl(StorageType storageType, FileResource resource) {
        return uploadUrl(storageType, resource.getResourceKey());
    }

    public PreUploadUrlResponse uploadUrl(StorageType storageType, ResourceKey resourceKey) {
        var filePath = fileResourceService.filePath(storageType, resourceKey);
        var preSignedUrl = fileStorageService.generatePreSignedUrl(filePath);
        return PreUploadUrlResponse.of(preSignedUrl, resourceKey);
    }
}
