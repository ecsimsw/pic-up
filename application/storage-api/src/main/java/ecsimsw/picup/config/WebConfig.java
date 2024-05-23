package ecsimsw.picup.config;

import ecsimsw.picup.resolver.RemoteIpArgumentResolver;
import ecsimsw.picup.resolver.ResourceKeyArgumentResolver;
import ecsimsw.picup.resolver.SearchCursorArgumentResolver;
import ecsimsw.picup.service.AuthTokenArgumentResolver;
import ecsimsw.picup.service.AuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthTokenService authTokenService;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new RemoteIpArgumentResolver());
        resolvers.add(new SearchCursorArgumentResolver());
        resolvers.add(new ResourceKeyArgumentResolver());
        resolvers.add(new AuthTokenArgumentResolver(authTokenService));
    }
}
