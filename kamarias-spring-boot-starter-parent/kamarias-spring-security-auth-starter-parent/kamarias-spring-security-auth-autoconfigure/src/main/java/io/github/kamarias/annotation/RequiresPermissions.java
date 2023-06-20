package io.github.kamarias.annotation;



import io.github.kamarias.enums.LogicalEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2023/6/16 9:09
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface RequiresPermissions {

    /**
     * 需要校验的角色标识
     */
    String[] value() default {};

    /**
     * 验证逻辑：AND | OR，默认AND
     */
    LogicalEnum logical() default LogicalEnum.AND;

}
