package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PicUpMemberApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpMemberApplication.class);
        app.run(args);
    }
}
