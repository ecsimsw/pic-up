package ecsimsw.picup;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.service.FileEventOutboxService;
import ecsimsw.picup.member.dto.SignUpRequest;
import ecsimsw.picup.member.service.MemberService;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
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
    }
}

@RequiredArgsConstructor
@Component
class Dummy {

    private final MemberService memberService;
    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;

    @PostConstruct
    public void dummy() {
        memberService.signUp(new SignUpRequest("ecsimsw", "password"));

        var album = albumRepository.save(new Album(1L, "hi", "hi", 2L));
        pictureRepository.save(new Picture(album, "sdf", "dsf", 2L));
        pictureRepository.save(new Picture(album, "sdf", "dsf", 2L));
        pictureRepository.save(new Picture(album, "sdf", "dsf", 2L));
        var save = pictureRepository.save(new Picture(album, "sdf", "dsf", 2L));

        List<Picture> all = pictureRepository.findAll();
        for(var p : all) {
            System.out.println(p.getAlbum() + " " + p.getCreatedAt());
        }

        List<Picture> allByAlbumOrderThan = pictureRepository.findAllByAlbumOrderThan(album.getId(), save.getId(), save.getCreatedAt(), PageRequest.of(0,1 ));
        System.out.println(allByAlbumOrderThan);
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
            .allowCredentials(true)
            .maxAge(3600);
    }
}
