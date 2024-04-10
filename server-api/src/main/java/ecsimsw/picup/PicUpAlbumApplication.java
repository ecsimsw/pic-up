package ecsimsw.picup;

import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.FileDeletionScheduler;
import ecsimsw.picup.album.service.FileStorageService;
import ecsimsw.picup.album.service.MemberService;
import ecsimsw.picup.storage.utils.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@EnableScheduling
@EnableRetry
@SpringBootApplication
public class PicUpAlbumApplication {

    public static void main(String[] args) {
//        var app = new SpringApplication(PicUpAlbumApplication.class);
//        app.setAdditionalProfiles("prod");

//        var ctx = app.run(args);
//        var outboxService = ctx.getBean(FileDeletionScheduler.class);
//        outboxService.schedulePublishOut();

//        FileStorageService bean = ctx.getBean(FileStorageService.class);
//        FileUtils.read("./storage-backup/");

        File dir = new File("./storage-backup/");
        String files[] = dir.list();
        for(String f : files) {
            System.out.println(f);
        }
    }
}
