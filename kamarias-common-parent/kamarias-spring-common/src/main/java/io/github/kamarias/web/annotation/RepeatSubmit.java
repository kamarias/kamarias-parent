package io.github.kamarias.web.annotation;

import java.lang.annotation.*;

/**
 * 重复提交限制注解
 * @author 王玉星
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

}
