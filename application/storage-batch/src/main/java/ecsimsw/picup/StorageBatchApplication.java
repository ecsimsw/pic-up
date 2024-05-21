package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

import static ecsimsw.picup.profile.ProfileUtils.profiles;

@EnableRetry
@SpringBootApplication
public class StorageBatchApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(StorageBatchApplication.class);
        app.setAdditionalProfiles(profiles(
            "storage-batch",
            "storage-core"
        ));
        app.run(args);
    }
}
