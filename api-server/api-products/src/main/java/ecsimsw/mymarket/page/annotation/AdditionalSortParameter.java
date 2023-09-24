package ecsimsw.mymarket.page.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.data.domain.Sort.Direction;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AdditionalSortParameter {
    String property() default "id";
    Direction direction() default Direction.ASC;
}
