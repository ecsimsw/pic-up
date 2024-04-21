package ecsimsw.picup.config;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import ecsimsw.picup.cdn.MockCloudFrontSignUrlSignService;
import ecsimsw.picup.cdn.UrlSignService;
import io.findify.s3mock.S3Mock;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class S3MockConfig {

    private final int port;
    private final S3Mock s3Mock;

    public S3MockConfig(
        @Value("${mock.object.storage.host.port}") int port
    ) {
        this.port = port;
        this.s3Mock = new S3Mock.Builder()
            .withPort(port)
            .withInMemoryBackend()
            .build();
    }

    @PostConstruct
    public void postConstruct() {
        s3Mock.start();
    }

    @PreDestroy
    public void preDestroy() {
        s3Mock.stop();
    }

    @Primary
    @Bean
    public UrlSignService mockSignUrlService() {
        return new MockCloudFrontSignUrlSignService();
    }

    @Primary
    @Bean
    public AmazonS3 amazonS3() {
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(
            "http://localhost:" + port, Regions.AP_NORTHEAST_2.getName());
        AmazonS3 client = AmazonS3ClientBuilder
            .standard()
            .withPathStyleAccessEnabled(true)
            .withEndpointConfiguration(endpoint)
            .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
            .build();
        client.createBucket(BUCKET_NAME);
        return client;
    }
}
