package ecsimsw.picup.album.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchCursor {

    String createdAtHeaderName() default "cursorCreatedAt";
    String limitHeaderName() default "limit";
    int limit() default 10;
}
