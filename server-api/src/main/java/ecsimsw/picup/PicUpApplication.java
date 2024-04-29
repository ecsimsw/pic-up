package ecsimsw.picup;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.MemberService;
import ecsimsw.picup.storage.S3Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;

@Log4j2
@EnableScheduling
@EnableRetry
@SpringBootApplication
public class PicUpApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(PicUpApplication.class);
        app.setAdditionalProfiles("prod");
        app.run(args);
    }
}
