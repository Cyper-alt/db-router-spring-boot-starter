package cn.bugstack.middleware.db.router.annotation;

import java.lang.annotation.*;

/**
 * @author Chen
 * @description
 * @createTime 2025/2/17 17:25
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DB {

    String key() default "";
}
