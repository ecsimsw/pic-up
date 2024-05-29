package ecsimsw.picup.service;

import static ecsimsw.picup.config.CacheManagerConfig.SIGNED_URL;
import static ecsimsw.picup.domain.StorageType.STORAGE;

import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.domain.StorageType;
import ecsimsw.picup.dto.PreUploadUrlResponse;
import ecsimsw.picup.exception.StorageException;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class FileUrlService {

    private static final String CDN_PROTOCOL = "https";
    private static final int SIGNED_URL_EXPIRATION_AFTER_DAYS = 7;

    @Value("${aws.cloudfront.domain}")
    private String domainName;

    @Value("${aws.cloudfront.publicKeyId}")
    private String publicKeyId;

    @Value("${aws.cloudfront.privateKeyPath}")
    private String privateKeyPath;

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
    private final ResourceService resourceService;
    private final FileStorage fileStorage;

    @Cacheable(value = SIGNED_URL, key = "{#storageType, #remoteIp, #fileResource.value()}")
    public String fileUrl(StorageType storageType, String remoteIp, ResourceKey fileResource) {
        System.out.println("type " + storageType);
        System.out.println("remote ip " + remoteIp);
        var filePath = resourceService.filePath(storageType, fileResource);
        var sign = cannedSign(remoteIp, filePath);
        var signedUrl = cloudFrontUtilities.getSignedUrlWithCustomPolicy(sign);
        return signedUrl.url();
    }

    private CustomSignerRequest cannedSign(String remoteIp, String resourcePath) {
        try {
            return CustomSignerRequest.builder()
                .privateKey(Path.of(privateKeyPath))
                .ipRange(remoteIp + "/32")
                .resourceUrl(new URL(CDN_PROTOCOL, domainName, "/" + resourcePath).toString())
                .keyPairId(publicKeyId)
                .expirationDate(Instant.now().plus(SIGNED_URL_EXPIRATION_AFTER_DAYS, ChronoUnit.DAYS))
                .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new StorageException("Failed to create cloudfront sign url from : " + remoteIp + ", " + resourcePath);
        }
    }

    public PreUploadUrlResponse preSignedUrl(ResourceKey resourceKey) {
        var filePath = resourceService.filePath(STORAGE, resourceKey);
        var preSignedUrl = fileStorage.generatePreSignedUrl(filePath);
        return PreUploadUrlResponse.of(preSignedUrl, resourceKey);
    }
}
