package ecsimsw.picup.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    private static final long MAX_FILE_SIZE = 200L;
    private static final long MAX_REQUEST_SIZE = 200L;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        var factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(MAX_FILE_SIZE));
        factory.setMaxRequestSize(DataSize.ofMegabytes(MAX_REQUEST_SIZE));
        return factory.createMultipartConfig();
    }
}
