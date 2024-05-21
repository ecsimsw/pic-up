package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static ecsimsw.picup.profile.ProfileUtils.profiles;

@SpringBootApplication
public class StorageApiApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(StorageApiApplication.class);
        app.setAdditionalProfiles(profiles(
            "storage-api",
            "storage-core",
            "auth-api",
            "logging-api")
        );
        app.run(args);
    }
}
