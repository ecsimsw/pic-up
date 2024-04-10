package ecsimsw.picup;

import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.FileDeletionScheduler;
import ecsimsw.picup.album.service.FileStorageService;
import ecsimsw.picup.album.service.MemberService;
import ecsimsw.picup.storage.service.ObjectStorage;
import ecsimsw.picup.storage.utils.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@EnableScheduling
@EnableRetry
@SpringBootApplication
public class PicUpAlbumApplication {

    public static void main(String[] args) throws IOException {
        var app = new SpringApplication(PicUpAlbumApplication.class);
        app.setAdditionalProfiles("prod");
//
        var ctx = app.run(args);
//        var outboxService = ctx.getBean(FileDeletionScheduler.class);
//        outboxService.schedulePublishOut();

//        FileStorageService bean = ctx.getBean(FileStorageService.class);
//        FileUtils.read("./storage-backup/");

        ObjectStorage bean = ctx.getBean(ObjectStorage.class);

        File dir = new File("./storage-backup/");
        for(File f : dir.listFiles()) {
            System.out.println("upload : "+ f.getName());
            byte[] byteFile = Files.readAllBytes(f.toPath());
            bean.storeAsync(
                f.getName(),
                new FileUploadResponse(
                    f.getName(),
                    PictureFileExtension.fromFileName(f.getName()),
                    byteFile.length,
                    byteFile
                    )
            );
            System.out.println(f);
        }
    }
}
