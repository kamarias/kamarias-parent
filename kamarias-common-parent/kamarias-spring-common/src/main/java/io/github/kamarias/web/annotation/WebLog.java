package io.github.kamarias.web.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 自定义日志切面
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/28 17:19
 */
@Inherited
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLog {

    /**
     * 自定义日志内容
     */
    @AliasFor("info")
    String value() default "";

    /**
     * 自定义日志内容
     */
    @AliasFor("value")
    String info() default "";

}
