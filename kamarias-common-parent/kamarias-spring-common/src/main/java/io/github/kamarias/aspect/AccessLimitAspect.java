package io.github.kamarias.aspect;


import io.github.kamarias.annotation.AccessLimit;
import io.github.kamarias.lock.DistributedLock;
import io.github.kamarias.utils.encrypt.Md5Utils;
import io.github.kamarias.utils.http.IpUtils;
import io.github.kamarias.utils.http.ServletUtils;
import io.github.kamarias.utils.string.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 限流访问切面
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/29 10:20
 */
@Aspect
public class AccessLimitAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessLimitAspect.class);

    private final StringRedisTemplate stringRedisTemplate;

    private final DistributedLock distributedLock;

    @Value("${spring.application.name:app}")
    private String APP_NAME;

    private final String errResponse = "{\"msg\":\"系统繁忙，请稍后再试\",\"code\":500}";

    public AccessLimitAspect(StringRedisTemplate stringRedisTemplate, DistributedLock distributedLock) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.distributedLock = distributedLock;
    }

    @Pointcut("@annotation(io.github.kamarias.annotation.AccessLimit)")
    public void accessLimitPointcut() {
    }

    @Around(value = "accessLimitPointcut()")
    public Object accessLimitPointcut(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        AccessLimit accessLimit = AnnotationUtils.getAnnotation(method, AccessLimit.class);
        // 类名加方法名
        final String methodName = StringUtils.join(method.getDeclaringClass().getSimpleName(), ".", method.getName());
        final String key = this.genCacheKey(methodName);
        final String lockKey = key + ":accessLimitLock";
        try {
            distributedLock.lock(lockKey);
            String frequency = this.stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.isNotEmpty(frequency)){
                // 存在限流次数
                if (Integer.valueOf(frequency) > 0){
                    Object proceed = pjp.proceed();
                    // 次数减 1
                    this.stringRedisTemplate.opsForValue().decrement(key, 1);
                    return proceed;
                }
                ServletUtils.renderString(ServletUtils.getResponse(), this.errResponse);
                LOGGER.warn("当前Ip被限流", new RuntimeException("frequent request is not allow"));
                return null;
            }else {
                // 不存在限流次数，加入限流
                Object proceed = pjp.proceed();
                // 设置 剩余
                this.stringRedisTemplate.opsForValue().set(key, String.valueOf(accessLimit.frequency() - 1), accessLimit.expireTime(), accessLimit.unit());
                return proceed;
            }
        }finally {
            distributedLock.releaseLock(lockKey);
        }
    }


    /**
     * 生成缓存key
     * @return 生成限流key
     */
    private String genCacheKey(String methodName) {
        String ip = IpUtils.getIpAdrress(ServletUtils.getRequest());
        List<String> keys = Arrays.asList(this.APP_NAME, AccessLimit.class.getSimpleName(), methodName, Md5Utils.getMD5(ip.getBytes()));
        return StringUtils.join(keys, ":");
    }

}
