package ecsimsw.picup;

import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.service.ImageEventOutboxService;
import ecsimsw.picup.member.domain.MemberRepository;
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
        var outboxService = ctx.getBean(ImageEventOutboxService.class);
        outboxService.schedulePublishOut();

        Dummy dummy = ctx.getBean(Dummy.class);
        dummy.saveMember();

    }
}

@RequiredArgsConstructor
@Component
class Dummy {

    private final MemberRepository repository;
    private final AlbumRepository albumRepository;
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
            .allowedOrigins("http://www.ecsimsw.com:8082", "http://121.185.9.18:8082", "www.ecsimsw.com:8082")
            .allowedMethods("*")
            .allowedHeaders("*")
            .maxAge(3600);
    }
}
