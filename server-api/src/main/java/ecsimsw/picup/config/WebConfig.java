package ecsimsw.picup.config;

import ecsimsw.picup.album.controller.SearchCursorArgumentResolver;
import ecsimsw.picup.album.controller.UserIpArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserIpArgumentResolver());
        resolvers.add(new SearchCursorArgumentResolver());
    }
}
