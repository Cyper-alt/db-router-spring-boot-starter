package cn.bugstack.middleware.db.router.annotation;

import java.lang.annotation.*;


/**
 * @author Chen
 * @description
 * @createTime 2024/12/18 20:18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouter {

    String key() default "";

}
