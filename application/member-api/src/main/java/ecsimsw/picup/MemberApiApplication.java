package ecsimsw.picup;

import static ecsimsw.picup.profile.ProfileUtils.profilesFromModules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MemberApiApplication {

    private static final String[] PROFILES = profilesFromModules("member-api", "member-core", "auth", "logging");

    public static void main(String[] args) {
        var app = new SpringApplication(MemberApiApplication.class);
        app.setAdditionalProfiles(profilesFromModules(PROFILES));
        app.run(args);
    }
}