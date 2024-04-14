package ecsimsw.picup.ecrypt;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;

import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static ecsimsw.picup.config.S3Config.ROOT_PATH;

@RequiredArgsConstructor
public class CloudFrontSignUrlService implements ResourceSignUrlService {

    private static final String CDN_PROTOCOL = "https";
    private static final int EXPIRATION_AFTER_DAYS = 7;

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
    private final String domainName;
    private final String publicKeyId;
    private final String privateKeyPath;

    @Override
    public String signedUrl(String remoteIp, String resourcePath) {
        try {
            System.out.println("remote Ip : " + remoteIp);
            var sign = cannedSign(remoteIp, ROOT_PATH + resourcePath);
            var signedUrl = cloudFrontUtilities.getSignedUrlWithCustomPolicy(sign);
            return signedUrl.url();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create cloudfront sign url from : " + resourcePath);
        }
    }

    private CustomSignerRequest cannedSign(String remoteIp, String resourcePath) throws Exception {
        return CustomSignerRequest.builder()
            .privateKey(Path.of(privateKeyPath))
            .ipRange(remoteIp + "/32")
            .resourceUrl(new URL(CDN_PROTOCOL, domainName, "/" + resourcePath).toString())
            .keyPairId(publicKeyId)
            .expirationDate(Instant.now().plus(EXPIRATION_AFTER_DAYS, ChronoUnit.DAYS))
            .build();
    }
}
