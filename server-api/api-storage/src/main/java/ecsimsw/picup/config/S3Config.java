package ecsimsw.picup.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class S3Config {

    @Primary
    @ConditionalOnProperty(value = "mock.object.storage.enable", havingValue = "false", matchIfMissing = true)
    @Bean
    public AmazonS3 objectStorageClient(
        @Value("${object.storage.credential.accessKey}") String accessKey,
        @Value("${object.storage.credential.secretKey}") String secretKey
    ) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(
            accessKey,
            secretKey
        );
        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
            .withRegion(Regions.AP_NORTHEAST_2)
            .build();
    }

    @ConditionalOnProperty(value = "mock.object.storage.enable", havingValue = "true")
    @Bean
    public AmazonS3 mockObjectStorageClient(
        @Value("${mock.object.storage.host.url}") String hostUrl,
        @Value("${mock.object.storage.host.port}") int port,
        @Value("${mock.object.storage.host.region}") String region,
        @Value("${object.storage.bucket.name}") String bucketName
    ) {
        var api = new S3Mock.Builder().withPort(port).withInMemoryBackend().build();
        api.start();

        var endpoint = new AwsClientBuilder.EndpointConfiguration(hostUrl + ":" + port, region);
        var amazonS3 = AmazonS3ClientBuilder
            .standard()
            .withPathStyleAccessEnabled(true)
            .withEndpointConfiguration(endpoint)
            .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
            .build();
        amazonS3.createBucket(bucketName);
        return amazonS3;
    }
}
