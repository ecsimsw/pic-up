package ecsimsw.picup.config.dev;

import ecsimsw.picup.album.service.FileUrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static ecsimsw.picup.config.CacheType.SIGNED_URL;

@Primary
@Profile("dev")
@Service
public class MockFileUrlService extends FileUrlService {

    public MockFileUrlService(
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
    @Cacheable(value = SIGNED_URL, key = "{#remoteIp, #originUrl}")
    public String sign(String remoteIp, String originUrl) {
        return "http://localhost:8084/" + originUrl;
    }
}
