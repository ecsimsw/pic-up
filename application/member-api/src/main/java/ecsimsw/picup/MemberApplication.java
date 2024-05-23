package ecsimsw.picup;

import static ecsimsw.picup.profile.ProfileUtils.profiles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MemberApplication {


    public static void main(String[] args) {
        var app = new SpringApplication(MemberApplication.class);
        app.setAdditionalProfiles(profiles(
            "member-api",
            "member-core",
            "auth",
            "logging"
        ));
        app.run(args);
    }
}