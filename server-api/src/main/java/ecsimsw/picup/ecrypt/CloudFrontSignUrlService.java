package ecsimsw.picup.ecrypt;

import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;

@RequiredArgsConstructor
public class CloudFrontSignUrlService implements ResourceSignUrlService {

    private static final String CDN_PROTOCOL = "https";
    private static final int EXPIRATION_AFTER_DAYS = 7;

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
    private final String domainName;
    private final String publicKeyId;
    private final String privateKeyPath;

    @Override
    public String signedUrl(String fileName) {
        try {
            var sign = cannedSign(fileName);
            var signedUrl = cloudFrontUtilities.getSignedUrlWithCustomPolicy(sign);
            return signedUrl.url();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create cloudfront sign url from : " + fileName);
        }
    }

    private CustomSignerRequest cannedSign(String fileName) throws Exception {
        return CustomSignerRequest.builder()
            .privateKey(Path.of(privateKeyPath))
            .resourceUrl(new URL(CDN_PROTOCOL, domainName, "/" + fileName).toString())
            .keyPairId(publicKeyId)
            .expirationDate(Instant.now().plus(EXPIRATION_AFTER_DAYS, ChronoUnit.DAYS))
            .build();
    }
}
