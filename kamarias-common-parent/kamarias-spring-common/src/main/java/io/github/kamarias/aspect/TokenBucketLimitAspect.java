package io.github.kamarias.aspect;


import com.google.common.util.concurrent.RateLimiter;
import io.github.kamarias.annotation.TokenBucketLimit;
import io.github.kamarias.utils.encrypt.Md5Utils;
import io.github.kamarias.utils.http.ServletUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 令牌桶限流算法
 * @author wangyuxing@gogpay.cn
 * @date @DATE @TIME
 */
@Aspect
public class TokenBucketLimitAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenBucketLimitAspect.class);


    private ConcurrentHashMap<String, RateLimiter> RATE_LIMITER  = new ConcurrentHashMap<>();


    private final String errResponse = "{\"msg\":\"系统繁忙，请稍后再试\",\"code\":500}";


    @Pointcut("@annotation(io.github.kamarias.annotation.TokenBucketLimit)")
    public void tokenBucketLimitPointcut() {
    }

    @Around(value = "tokenBucketLimitPointcut()")
    public Object accessLimitPointcut(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        TokenBucketLimit limit = AnnotationUtils.getAnnotation(method, TokenBucketLimit.class);
        TokenBucketLimit.TokenBucketMode tokenBucketMode = limit.mode();
        String value = limit.value();
        double permitsPerSecond = limit.permitsPerSecond();
        String key = this.genLimitKey(method, value);
        RateLimiter rateLimiter = RATE_LIMITER.get(key);
        // 不存在限流key
        if (rateLimiter == null){
            if (TokenBucketLimit.TokenBucketMode.WARM_UP.equals(tokenBucketMode)){
                long warmupPeriod = limit.warmupPeriod();
                TimeUnit warmupUnit = limit.warmupUnit();
                double coldFactor = limit.coldFactor();
                // 预热模式
                rateLimiter = RateLimiter.create(permitsPerSecond,warmupPeriod,warmupUnit);
                Class calss = rateLimiter.getClass();
                Field field = calss.getDeclaredField("coldFactor");
                field.setAccessible(true);
                field.set(rateLimiter, coldFactor);
                rateLimiter.setRate(permitsPerSecond);
            }else {
                // 满载模式
                long tokenMaxTime = limit.tokenMaxTime();
                rateLimiter = RateLimiter.create(permitsPerSecond);
                Class calss = rateLimiter.getClass();
                Field field = calss.getDeclaredField("maxBurstSeconds");
                field.setAccessible(true);
                field.set(rateLimiter, tokenMaxTime);
                rateLimiter.setRate(permitsPerSecond);

            }
            RATE_LIMITER.put(key, rateLimiter);
        }
        // 等待令牌时间
        long waitTokenTime = limit.waitTokenTime();
        if(rateLimiter.tryAcquire(waitTokenTime, TimeUnit.MILLISECONDS)){
            // 在等待时间内获取到令牌就执行方法
            return pjp.proceed();
        }else{
            ServletUtils.renderString(ServletUtils.getResponse(), this.errResponse);
            LOGGER.warn("获取令牌失败");
            return null;
        }
    }

    private String genLimitKey(Method method,String value) {
        String limitKey =  method.getDeclaringClass().getTypeName() + method.getName() + value;
        return Md5Utils.getMD5(limitKey.getBytes());
    }

}
