package ecsimsw.mymarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyMarketPictureApplication {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(MyMarketPictureApplication.class);
    app.setAdditionalProfiles("dev");
    app.run(args);
  }
}
