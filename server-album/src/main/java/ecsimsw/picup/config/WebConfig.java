package ecsimsw.picup.config;

import ecsimsw.picup.controller.RemoteIpArgumentResolver;
import ecsimsw.picup.controller.ResourceKeyArgumentResolver;
import ecsimsw.picup.controller.SearchCursorArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new RemoteIpArgumentResolver());
        resolvers.add(new SearchCursorArgumentResolver());
        resolvers.add(new ResourceKeyArgumentResolver());
    }
}
