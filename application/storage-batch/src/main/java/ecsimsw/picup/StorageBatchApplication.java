package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

import static ecsimsw.picup.profile.ProfileUtils.profilesFromModules;

@EnableRetry
@SpringBootApplication
public class StorageBatchApplication {

    private static final String[] PROFILES = profilesFromModules("storage-batch", "storage-core");

    public static void main(String[] args) {
        var app = new SpringApplication(StorageBatchApplication.class);
        app.setAdditionalProfiles(PROFILES);
        app.run(args);
    }
}
