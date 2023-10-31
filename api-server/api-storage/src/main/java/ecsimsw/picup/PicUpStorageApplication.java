package ecsimsw.picup;

import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootApplication
public class PicUpStorageApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpStorageApplication.class);
        app.setAdditionalProfiles("dev");
        ConfigurableApplicationContext run = app.run(args);

        ResourceRepository bean = run.getBean(ResourceRepository.class);
        var resource = bean.save(Resource.createRequested("tag", new MockMultipartFile("hi.jpg", "string".getBytes(StandardCharsets.UTF_8))));
        var saved = bean.findById(resource.getResourceKey()).orElseThrow();

        var saved2 = bean.save(new Resource(saved.getResourceKey(), saved.getStoredStorages(), null, saved.getDeleteRequested()));
        System.out.println("fff " + saved.getCreateRequested());
        System.out.println("fff " + saved2.getCreateRequested());


    }
}
