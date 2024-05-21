package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.AbstractEnvironment;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static ecsimsw.picup.profile.ProfileUtils.profiles;

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