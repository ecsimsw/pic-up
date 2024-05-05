package ecsimsw.picup.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import ecsimsw.picup.album.domain.StorageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@Configuration
public class S3Config {

    public static final long PRE_SIGNED_URL_EXPIRATION_MS = 10_000;
    public static final String BUCKET = "picup-ecsimsw";

    public static final String ROOT_PATH_STORAGE = "storage/";
    public static final String ROOT_PATH_THUMBNAIL = "thumb/";
    public static final String DEFAULT_VIDEO_THUMBNAIL_EXTENSION = "jpg";

    public static final Map<StorageType, String> ROOT_PATH_PER_STORAGE_TYPE = Map.of(
        STORAGE, ROOT_PATH_STORAGE,
        THUMBNAIL, ROOT_PATH_THUMBNAIL
    );

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
