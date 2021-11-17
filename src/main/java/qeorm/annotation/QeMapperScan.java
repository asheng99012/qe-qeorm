package qeorm.annotation;

import org.springframework.context.annotation.Import;
import qeorm.QeMapperAutoConfigureRegistrar;

import java.lang.annotation.*;

/**
 * Created by asheng on 2015/7/20 0020.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(QeMapperAutoConfigureRegistrar.class)
public @interface QeMapperScan {
    String[] value() default {};
}
