package io.github.kamarias.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 令牌桶算法限流
 * @author wangyuxing@gogpay.cn
 * @date @DATE @TIME
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TokenBucketLimit {

    /**
     * 限流名字（limitName） 别名 name
     */
    @AliasFor("name")
    String value() default "";

    /**
     * 限流名字（limitName）别名 value
     */
    @AliasFor("value")
    String name() default "";

    /**
     * (预热模式下不生效，因为预热模式的最大值是动态的)
     * 令牌可用最大时间
     * 通过设置令牌的最大可用时间，可调节令牌桶中令牌最大数量
     * 即 permitsPerSecond * tokenMaxTime = tokenBucketCapacity
     * 每秒令牌数 * 令牌最大可用时间 = 令牌桶最大容量
     */
    long tokenMaxTime() default 1;

    /**
     * 每秒令牌通生成数量
     */
    double permitsPerSecond() default 20;

    /**
     * 等待令牌时间（单位毫秒）
     * 即尝试获取令牌时间，没有在规定时间类没有获取令牌直接返回错误
     */
    long waitTokenTime() default 100;

    /**
     * (预热模式下生效)
     * 预热时间 TokenBucketLimit.mode() 为 TokenBucketMode.WARM_UP 时生效
     */
    long warmupPeriod() default 3;

    /**
     * (预热模式下生效)
     * 预热单位 TokenBucketLimit.mode() 为 TokenBucketMode.WARM_UP 时生效
     */
    TimeUnit warmupUnit() default TimeUnit.SECONDS;

    /**
     * (预热模式下生效)
     * 寒冷因子
     * 访问量较低时：令牌桶生成最大令牌数是 permitsPerSecond/coldFactor
     * 即 20 / 3 = 7（四舍五入）最大令牌数为：7
     */
    double coldFactor() default 3;

    /**
     * 令牌桶启动模式
     */
    TokenBucketMode mode() default TokenBucketMode.ALL_LOAD;

    enum TokenBucketMode{
        /**
         * 满载
         */
        ALL_LOAD,

        /**
         *  预热模式
         */
        WARM_UP
    }

}
