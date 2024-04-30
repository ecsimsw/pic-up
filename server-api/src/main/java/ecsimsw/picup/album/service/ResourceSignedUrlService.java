package ecsimsw.picup.album.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;

import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class ResourceSignedUrlService {

    private static final String CDN_PROTOCOL = "https";
    private static final int EXPIRATION_AFTER_DAYS = 7;

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
    private final String domainName;
    private final String publicKeyId;
    private final String privateKeyPath;

    public ResourceSignedUrlService(
        @Value("${aws.cloudfront.domain}")
        String domainName,
        @Value("${aws.cloudfront.publicKeyId}")
        String publicKeyId,
        @Value("${aws.cloudfront.privateKeyPath}")
        String privateKeyPath
    ) {
        this.domainName = domainName;
        this.publicKeyId = publicKeyId;
        this.privateKeyPath = privateKeyPath;
    }

    public String sign(String remoteIp, String resourcePath) {
        try {
            var sign = cannedSign(remoteIp, resourcePath);
            var signedUrl = cloudFrontUtilities.getSignedUrlWithCustomPolicy(sign);
            return signedUrl.url();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create cloudfront sign url from : " + resourcePath);
        }
    }

    private CustomSignerRequest cannedSign(String remoteIp, String resourcePath) throws Exception {
        return CustomSignerRequest.builder()
            .privateKey(Path.of(privateKeyPath))
//            .ipRange(remoteIp + "/32")
            .resourceUrl(new URL(CDN_PROTOCOL, domainName, "/" + resourcePath).toString())
            .keyPairId(publicKeyId)
            .expirationDate(Instant.now().plus(EXPIRATION_AFTER_DAYS, ChronoUnit.DAYS))
            .build();
    }
}
