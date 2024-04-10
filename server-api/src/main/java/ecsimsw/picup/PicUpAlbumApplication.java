package ecsimsw.picup;

import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.storage.service.ObjectStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;

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

        ObjectStorage bean = (ObjectStorage) ctx.getBean("mainStorage");
        byte[] byteFile = new byte[0];
        File dir = new File("./storage-backup/");
        for (File f : dir.listFiles()) {
            System.out.println("upload : " + f.getName());
            bean.storeAsync(
                f.getName(),
                new FileUploadResponse(
                    f.getName(),
                    PictureFileExtension.fromFileName(f.getName()),
                    byteFile.length,
                    byteFile
                    )
            );
        }
    }
}
