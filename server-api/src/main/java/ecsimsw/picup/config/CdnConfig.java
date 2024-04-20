package ecsimsw.picup.config;

import ecsimsw.picup.cdn.CloudFrontSignUrlSignService;
import ecsimsw.picup.cdn.MockCloudFrontSignUrlSignService;
import ecsimsw.picup.cdn.UrlSignService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CdnConfig {

    @Profile("prod")
    @Bean
    public UrlSignService signUrlService(
        @Value("${aws.cloudfront.domain}")
        String domainName,
        @Value("${aws.cloudfront.publicKeyId}")
        String publicKeyId,
        @Value("${aws.cloudfront.privateKeyPath}")
        String privateKeyPath
    ) {
        return new CloudFrontSignUrlSignService(domainName, publicKeyId, privateKeyPath);
    }

    @Profile("dev")
    @Bean
    public UrlSignService mockSignUrlService() {
        return new MockCloudFrontSignUrlSignService();
    }
}
