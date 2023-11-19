package ecsimsw.picup.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    private static final int MAX_FILE_SIZE_MB = 200;
    private static final int MAX_REQUEST_SIZE_MB = 200;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        final MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(MAX_FILE_SIZE_MB));
        factory.setMaxRequestSize(DataSize.ofMegabytes(MAX_REQUEST_SIZE_MB));
        return factory.createMultipartConfig();
    }
}
