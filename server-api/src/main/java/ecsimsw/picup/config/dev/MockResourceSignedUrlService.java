package ecsimsw.picup.config.dev;

import ecsimsw.picup.album.service.ResourceSignedUrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Primary
@Profile("dev")
@Configuration
public class MockResourceSignedUrlService extends ResourceSignedUrlService {

    public MockResourceSignedUrlService(
        @Value("${aws.cloudfront.domain}")
        String domainName,
        @Value("${aws.cloudfront.publicKeyId}")
        String publicKeyId,
        @Value("${aws.cloudfront.privateKeyPath}")
        String privateKeyPath
    ) {
        super(domainName, publicKeyId, privateKeyPath);
    }

    @Override
    public String sign(String remoteIp, String fileName) {
        return "http://localhost:8084/" + fileName;
    }
}
