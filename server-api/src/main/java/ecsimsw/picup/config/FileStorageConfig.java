package ecsimsw.picup.config;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import ecsimsw.picup.storage.service.FileStorage;
import ecsimsw.picup.storage.service.ImageStorage;
import ecsimsw.picup.storage.service.ObjectStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig {

    public static final int UPLOAD_TIME_OUT_SEC = 5;
    public static final String FILE_STORAGE_PATH = "./storage-backup/";

    @Bean
    public ImageStorage mainStorage() {
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withRegion(Regions.AP_NORTHEAST_2)
            .build();
        return new ObjectStorage(s3);
    }

    @Bean
    public ImageStorage backUpStorage() {
        return new FileStorage(FILE_STORAGE_PATH);
    }
}
