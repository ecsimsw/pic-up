package ecsimsw.picup.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import ecsimsw.picup.ecrypt.CloudFrontSignUrlService;
import ecsimsw.picup.ecrypt.MockCloudFrontSignUrlService;
import ecsimsw.picup.ecrypt.ResourceSignUrlService;
import io.findify.s3mock.S3Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class S3Config {

    @Primary
    @ConditionalOnProperty(value = "aws.cloudfront.sign", havingValue = "false", matchIfMissing = true)
    @Bean
    public ResourceSignUrlService mockSignUrlService() {
        return new MockCloudFrontSignUrlService();
    }

    @ConditionalOnProperty(value = "aws.cloudfront.sign", havingValue = "true")
    @Bean
    public ResourceSignUrlService signUrlService(
        @Value("${aws.cloudfront.domain}")
        String domainName,
        @Value("${aws.cloudfront.publicKeyId}")
        String publicKeyId,
        @Value("${aws.cloudfront.privateKeyPath}")
        String privateKeyPath
    ) {
        return new CloudFrontSignUrlService(
            domainName,
            publicKeyId,
            privateKeyPath
        );
    }
}
