package ecsimsw.picup;

import ecsimsw.picup.album.service.FileDeletionScheduler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableRetry
@SpringBootApplication
public class PicUpAlbumApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(PicUpAlbumApplication.class);
        app.setAdditionalProfiles("prod");
        var ctx = app.run(args);
        var outboxService = ctx.getBean(FileDeletionScheduler.class);
        outboxService.schedulePublishOut();
    }
}
