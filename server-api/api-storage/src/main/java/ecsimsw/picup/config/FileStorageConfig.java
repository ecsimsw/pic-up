package ecsimsw.picup.config;

import ecsimsw.picup.service.FileStorage;
import ecsimsw.picup.service.ImageStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig {

    public static final int UPLOAD_TIME_OUT_SEC = 5;

    private static final String MAIN_STORAGE_KEY = "MAIN_STORAGE";
    private static final String MAIN_STORAGE_PATH = "./storage";

    private static final String BACKUP_STORAGE_KEY = "BACKUP_STORAGE";
    private static final String BACKUP_STORAGE_PATH = "./storage-backup";

    @Bean
    public ImageStorage mainStorage() {
        return new FileStorage(MAIN_STORAGE_KEY, MAIN_STORAGE_PATH);
    }

    @Bean
    public ImageStorage backUpStorage() {
        return new FileStorage(BACKUP_STORAGE_KEY, BACKUP_STORAGE_PATH);
    }
}
