package ecsimsw.picup;

import ecsimsw.picup.album.service.FileEventOutboxService;
import ecsimsw.picup.member.dto.SignUpRequest;
import ecsimsw.picup.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableScheduling
@EnableRetry
@SpringBootApplication
public class PicUpAlbumApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(PicUpAlbumApplication.class);
        app.setAdditionalProfiles("dev");
        var ctx = app.run(args);
        var outboxService = ctx.getBean(FileEventOutboxService.class);
        outboxService.schedulePublishOut();

        Dummy dummy = ctx.getBean(Dummy.class);
        dummy.saveMember();

    }
}

@RequiredArgsConstructor
@Component
class Dummy {

    private final MemberService service;

    public void saveMember() {
        service.signUp(new SignUpRequest("ecsimsw", "hihihihhihi"));
    }

}

@Configuration
class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:63342")
            .allowedMethods("*")
            .allowedHeaders("*")
            .maxAge(3600);
    }
}
