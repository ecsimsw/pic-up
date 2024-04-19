package ecsimsw.picup.config;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import ecsimsw.picup.utils.PortUtils;
import io.findify.s3mock.S3Mock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class S3MockConfig {

    private static final int PORT_MIN = 9000;
    private static final int PORT_MAX = 9090;

    @Primary
    @Bean
    public AmazonS3 amazonS3(){
        int port = findPort();
        S3Mock s3Mock = new S3Mock.Builder()
            .withPort(port)
            .withInMemoryBackend()
            .build();
        s3Mock.start();
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration("http://localhost:"+port, Regions.AP_NORTHEAST_2.getName());
        AmazonS3 client = AmazonS3ClientBuilder
            .standard()
            .withPathStyleAccessEnabled(true)
            .withEndpointConfiguration(endpoint)
            .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
            .build();
        client.createBucket(BUCKET_NAME);
        return client;
    }

    public int findPort() {
        for(int port = PORT_MIN; port<PORT_MAX; port++) {
            if(PortUtils.checkPortAvailable(port)) {
                return port;
            }
        }
        throw new IllegalArgumentException("Failed to find port");
    }
}
