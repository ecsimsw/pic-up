package ecsimsw.mymarket.page;

import javax.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OsivAdminConfig implements WebMvcConfigurer {

    private final EntityManagerFactory entityManagerFactory;

    public OsivAdminConfig(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        var osivInterceptor = new OpenEntityManagerInViewInterceptor();
        osivInterceptor.setEntityManagerFactory(entityManagerFactory);
        registry.addWebRequestInterceptor(osivInterceptor)
                .addPathPatterns("/api/products/admin/**");
    }
}
