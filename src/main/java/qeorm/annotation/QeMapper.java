package qeorm.annotation;

import java.lang.annotation.*;

/**
 * Created by asheng on 2015/7/20 0020.
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QeMapper   {
    String value() default "";
}
