package ecsimsw.picup.config;

import ecsimsw.picup.album.service.ResourceSignedUrlService;
import ecsimsw.picup.dev.MockResourceSignedUrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class CdnConfig {

    @Bean
    public ResourceSignedUrlService signUrlService(
        @Value("${aws.cloudfront.domain}")
        String domainName,
        @Value("${aws.cloudfront.publicKeyId}")
        String publicKeyId,
        @Value("${aws.cloudfront.privateKeyPath}")
        String privateKeyPath
    ) {
        return new ResourceSignedUrlService(domainName, publicKeyId, privateKeyPath);
    }

    @Primary
    @Profile("dev")
    @Bean
    public ResourceSignedUrlService mockSignUrlService() {
        return new MockResourceSignedUrlService(
            "localhost:8084",
            "publicKeyId",
            "./privateKey"
        );
    }
}
