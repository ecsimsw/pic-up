package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static ecsimsw.picup.profile.ProfileUtils.profilesFromModules;

@SpringBootApplication
public class StorageApiApplication {

    private static final String[] PROFILES = profilesFromModules("storage-api", "storage-core", "auth", "logging", "common");

    public static void main(String[] args) {
        var app = new SpringApplication(StorageApiApplication.class);
        app.setAdditionalProfiles(PROFILES);
        app.run(args);
    }
}
