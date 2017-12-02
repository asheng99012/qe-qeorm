package qeorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by asheng on 2015/7/20 0020.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String masterDbName() default "";

    String slaveDbName() default "";

    String primaryKey() default "id";

    String tableName() default "";

    String where() default "";
}
