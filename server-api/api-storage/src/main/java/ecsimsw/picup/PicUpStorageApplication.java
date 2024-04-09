package ecsimsw.picup;

import ecsimsw.picup.domain.StoredFile;
import ecsimsw.picup.domain.StoredFileType;
import ecsimsw.picup.service.ObjectStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class PicUpStorageApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpStorageApplication.class);
        app.setAdditionalProfiles("dev");
        var ctx = app.run(args);
        ctx.getBean(Test.class).run();
    }
}

@Component
class Test {

    private final ObjectStorage objectStorage;

    public Test(ObjectStorage objectStorage) {
        this.objectStorage = objectStorage;

    }

    public void run() {
        this.objectStorage.storeAsync(
            "teseet.jpg",
            new StoredFile(StoredFileType.JPG, 1, new byte[] {1})
        );
    }
}
