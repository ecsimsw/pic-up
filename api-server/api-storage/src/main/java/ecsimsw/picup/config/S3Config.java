package ecsimsw.picup.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.S3DirectSpi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Bean
    public S3DirectSpi s3Client(
        @Value("${s3.vultr.host.url}") String hostUrl,
        @Value("${s3.vultr.host.region}") String region,
        @Value("${s3.vultr.credential.accessKey}") String accessKey,
        @Value("${s3.vultr.credential.secretKey}") String secretKey
    ) {
        AmazonS3ClientBuilder s3ClientBuilder = AmazonS3ClientBuilder.standard().withCredentials(
            new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))
        );
        EndpointConfiguration endPoint = new EndpointConfiguration(hostUrl, region);
        s3ClientBuilder.setEndpointConfiguration(endPoint);
        return s3ClientBuilder.build();
    }
}
