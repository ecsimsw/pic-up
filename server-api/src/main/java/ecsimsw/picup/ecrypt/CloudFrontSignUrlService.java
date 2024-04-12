package ecsimsw.picup.ecrypt;

import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.jcodec.common.logging.Logger;
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
    public String signedUrl(String remoteIp, String fileName) {
        try {
            var start = System.currentTimeMillis();
            var sign = cannedSign(remoteIp, fileName);
            var signedUrl = cloudFrontUtilities.getSignedUrlWithCustomPolicy(sign);
            Logger.info("signed url : " + (System.currentTimeMillis() - start));
            System.out.println("signed url : " + (System.currentTimeMillis() - start));
            return signedUrl.url();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create cloudfront sign url from : " + fileName);
        }
    }

    private CustomSignerRequest cannedSign(String remoteIp, String fileName) throws Exception {
        return CustomSignerRequest.builder()
            .privateKey(Path.of(privateKeyPath))
            .ipRange(remoteIp)
            .resourceUrl(new URL(CDN_PROTOCOL, domainName, "/" + fileName).toString())
            .keyPairId(publicKeyId)
            .expirationDate(Instant.now().plus(EXPIRATION_AFTER_DAYS, ChronoUnit.DAYS))
            .build();
    }
}
