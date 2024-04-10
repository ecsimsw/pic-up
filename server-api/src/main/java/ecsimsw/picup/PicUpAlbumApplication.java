package ecsimsw.picup;

import com.google.common.io.Files;
import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.storage.service.ObjectStorage;
import java.io.File;
import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableRetry
@SpringBootApplication
public class PicUpAlbumApplication {

    public static void main(String[] args) throws IOException {
        var app = new SpringApplication(PicUpAlbumApplication.class);
        app.setAdditionalProfiles("dev");

        var ctx = app.run(args);

        ObjectStorage bean = ctx.getBean(ObjectStorage.class);
        File dir = new File("./storage-backup/");
        for (File f : dir.listFiles()) {
            System.out.println("upload : " + f.getName());
            byte[] byteArray = Files.toByteArray(f);
            bean.storeAsync(
                f.getName(),
                new FileUploadResponse(
                    f.getName(),
                    PictureFileExtension.fromFileName(f.getName()),
                    byteArray.length,
                    byteArray
                )
            );
        }
    }
}
