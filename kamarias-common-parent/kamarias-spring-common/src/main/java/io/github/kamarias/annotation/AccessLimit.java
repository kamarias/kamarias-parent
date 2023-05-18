package io.github.kamarias.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 访问限制
 * 默认值：30秒内只能访问一次
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/29 10:15
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessLimit {

    /**
     * 访问次数
     */
    int frequency() default 1;

    /**
     * 限流时间
     */
    int expireTime() default 30;

    /**
     * 限流单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

}
