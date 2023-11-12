package ecsimsw.picup.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectStorageConfig {

    @Bean
    public AmazonS3 objectStorageClient(
        @Value("${object.storage.vultr.host.url}") String hostUrl,
        @Value("${object.storage.vultr.host.region}") String region,
        @Value("${object.storage.vultr.credential.accessKey}") String accessKey,
        @Value("${object.storage.vultr.credential.secretKey}") String secretKey
    ) {
        AmazonS3ClientBuilder s3ClientBuilder = AmazonS3ClientBuilder.standard().withCredentials(
            new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))
        );
        EndpointConfiguration endPoint = new EndpointConfiguration(hostUrl, region);
        s3ClientBuilder.setEndpointConfiguration(endPoint);
        return s3ClientBuilder.build();
    }
}
