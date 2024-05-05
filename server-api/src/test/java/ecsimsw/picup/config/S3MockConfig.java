//package ecsimsw.picup.config;
//
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.AnonymousAWSCredentials;
//import com.amazonaws.client.builder.AwsClientBuilder;
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import ecsimsw.picup.config.dev.MockFileUrlService;
//import io.findify.s3mock.S3Mock;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Primary;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//
//import static ecsimsw.picup.album.service.FileResourceService.BUCKET;
//
//@Slf4j
//@TestConfiguration
//public class S3MockConfig {
//
//    private final int port = 8002;
//    private final S3Mock s3Mock = new S3Mock.Builder()
//        .withPort(port)
//        .withInMemoryBackend()
//        .build();
//
//    @PostConstruct
//    public void postConstruct() {
//        log.info("==== Embedded S3 start ====");
//        s3Mock.start();
//    }
//
//    @PreDestroy
//    public void preDestroy() {
//        s3Mock.stop();
//    }
//
//    @Primary
//    @Bean
//    public MockFileUrlService mockSignUrlService() {
//        return new MockFileUrlService(
//            "http://localhost:8084",
//            "publicKeyId",
//            "privateKeyPath"
//        );
//    }
//
//    @Primary
//    @Bean
//    public AmazonS3 amazonS3() {
//        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(
//            "http://localhost:" + port, Regions.AP_NORTHEAST_2.getName());
//        AmazonS3 client = AmazonS3ClientBuilder
//            .standard()
//            .withPathStyleAccessEnabled(true)
//            .withEndpointConfiguration(endpoint)
//            .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
//            .build();
//        client.createBucket(BUCKET);
//        return client;
//    }
//}
