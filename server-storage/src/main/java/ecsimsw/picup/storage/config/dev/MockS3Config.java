package ecsimsw.picup.storage.config.dev;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import static ecsimsw.picup.storage.config.S3Config.BUCKET;

@Configuration
public class MockS3Config {

    private static final String MOCK_S3_HOST = "http://localhost";
    private static final int MOCK_S3_PORT = 8002;

    private S3Mock s3Mock;

    @Primary
    @Profile("dev")
    @Bean
    public AmazonS3 mockObjectStorageClient() {
        s3Mock = new S3Mock.Builder()
            .withPort(MOCK_S3_PORT)
            .withInMemoryBackend()
            .build();
        s3Mock.start();

        var endpoint = new AwsClientBuilder.EndpointConfiguration(
            MOCK_S3_HOST + ":" + MOCK_S3_PORT,
            Regions.AP_NORTHEAST_2.getName()
        );
        var amazonS3 = AmazonS3ClientBuilder.standard()
            .withPathStyleAccessEnabled(true)
            .withEndpointConfiguration(endpoint)
            .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
            .build();
        amazonS3.createBucket(BUCKET);
        return amazonS3;
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosedEvent() {
        if(s3Mock != null) {
            s3Mock.shutdown();
        }
    }
}
