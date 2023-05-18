package io.github.kamarias.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存注解，打上该注解的方法可走 redis 缓存
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/28 15:48
 *
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheableResponse {

    /**
     * 缓存时间
     */
    int expireTime() default 30;

    /**
     * 缓存单位
     */
    TimeUnit unit() default TimeUnit.MINUTES;

}
