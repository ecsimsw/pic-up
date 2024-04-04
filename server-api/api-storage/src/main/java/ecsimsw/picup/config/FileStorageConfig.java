package ecsimsw.picup.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import ecsimsw.picup.service.FileStorage;
import ecsimsw.picup.service.ImageStorage;
import io.findify.s3mock.S3Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FileStorageConfig {

    public static final int UPLOAD_TIME_OUT_SEC = 5;

    private static final String MAIN_STORAGE_KEY = "MAIN_STORAGE";
    public static final String MAIN_STORAGE_PATH = "./storage/";

    private static final String BACKUP_STORAGE_KEY = "BACKUP_STORAGE";
    public static final String BACKUP_STORAGE_PATH = "./storage-backup/";

    @Bean
    public ImageStorage mainStorage() {
        return new FileStorage(MAIN_STORAGE_KEY, MAIN_STORAGE_PATH);
    }

    @Bean
    public ImageStorage backUpStorage() {
        return new FileStorage(BACKUP_STORAGE_KEY, BACKUP_STORAGE_PATH);
    }

    @Primary
    @ConditionalOnProperty(value = "mock.object.storage.enable", havingValue = "false", matchIfMissing = true)
    @Bean
    public AmazonS3 objectStorageClient(
        @Value("${object.storage.host.url}") String hostUrl,
        @Value("${object.storage.host.region}") String region,
        @Value("${object.storage.credential.accessKey}") String accessKey,
        @Value("${object.storage.credential.secretKey}") String secretKey
    ) {
        var s3ClientBuilder = AmazonS3ClientBuilder.standard().withCredentials(
            new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))
        );
        var endPoint = new EndpointConfiguration(hostUrl, region);
        s3ClientBuilder.setEndpointConfiguration(endPoint);
        s3ClientBuilder.withClientConfiguration(
            new ClientConfiguration()
                .withConnectionTimeout(3000)
                .withSocketTimeout(3000)
                .withMaxConnections(100)
        );
        return s3ClientBuilder.build();
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

        var endpoint = new EndpointConfiguration(hostUrl + ":" + port, region);
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
