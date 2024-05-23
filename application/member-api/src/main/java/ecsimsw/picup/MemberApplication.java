package ecsimsw.picup;

import static ecsimsw.picup.profile.ProfileUtils.profilesFromModules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MemberApplication {

    private static final String[] PROFILES = profilesFromModules("member-api", "member-core", "auth", "logging");

    public static void main(String[] args) {
        var app = new SpringApplication(MemberApplication.class);
        app.setAdditionalProfiles(
            "member-api-dev", "member-core-dev", "auth-dev", "logging-dev"
        );
        app.run(args);
    }
}