package ecsimsw.picup.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    public static final String ROOT_PATH = "storage/";
    public static final String BUCKET_NAME = "picup-ecsimsw";

    @Bean
    public AmazonS3 objectStorageClient(
        @Value("${object.storage.credential.accessKey}")
        String accessKey,
        @Value("${object.storage.credential.secretKey}")
        String secretKey
    ) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
            .withRegion(Regions.AP_NORTHEAST_2)
            .build();
    }
}
