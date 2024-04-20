package ecsimsw.picup.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import ecsimsw.picup.cdn.CloudFrontSignUrlSignService;
import ecsimsw.picup.cdn.MockCloudFrontSignUrlSignService;
import ecsimsw.picup.cdn.UrlSignService;
import io.findify.s3mock.S3Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class S3Config {

    public static final String ROOT_PATH = "storage/";
    public static final String BUCKET_NAME = "picup-ecsimsw";

    @Profile("prod")
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

    @Profile("dev")
    @Bean
    public AmazonS3 mockObjectStorageClient(
        @Value("${mock.object.storage.host.url}") String hostUrl,
        @Value("${mock.object.storage.host.port}") int port
    ) {
        var api = new S3Mock.Builder()
            .withPort(port)
            .withInMemoryBackend()
            .build();
        api.start();

        var endpoint = new AwsClientBuilder.EndpointConfiguration(hostUrl + ":" + port, Regions.AP_NORTHEAST_2.getName());
        var amazonS3 = AmazonS3ClientBuilder
            .standard()
            .withPathStyleAccessEnabled(true)
            .withEndpointConfiguration(endpoint)
            .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
            .build();
        amazonS3.createBucket(BUCKET_NAME);
        return amazonS3;
    }
}
