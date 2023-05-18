package io.github.kamarias.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.kamarias.annotation.CacheableResponse;
import io.github.kamarias.cache.RedisCache;
import io.github.kamarias.lock.DistributedLock;
import io.github.kamarias.utils.encrypt.Md5Utils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 缓存注解切面
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/28 15:51
 */
@Aspect
public class CacheableResponseAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheableResponseAspect.class);

    @Value("${spring.profiles.active}")
    private String env;

    private final String ENV_DEV = "dev";

    private final RedisCache redisCache;

    private final DistributedLock distributedLock;

    @Value("${spring.application.name:app}")
    private String APP_NAME;

    public CacheableResponseAspect(RedisCache redisCache, DistributedLock distributedLock) {
        this.redisCache = redisCache;
        this.distributedLock = distributedLock;
    }

    @Pointcut("@annotation(io.github.kamarias.annotation.CacheableResponse)")
    public void pointcut() {
    }

    @Around(value = "pointcut()")
    public Object aroundProcess(ProceedingJoinPoint pjp) throws Throwable {
        // 开发环境直接通过
        if (this.ENV_DEV.equals(env)) {
            return pjp.proceed();
        }
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        // 类名加方法名
        final String methodFullName = StringUtils.join(method.getDeclaringClass().getSimpleName(), ".", method.getName());
        // 参数
        final Object[] args = pjp.getArgs();
        final String cacheKey = this.genCacheKey(args, methodFullName);
        String lockKey = cacheKey + ":cacheLock";
        try {
            distributedLock.lock(lockKey);
            // 返回类型
            final Class<?> returnClass = Class.forName(method.getGenericReturnType().getTypeName());
            final String cacheDataString = this.redisCache.getCacheObject(cacheKey);
            if (StringUtils.isNotBlank(cacheDataString) && returnClass != null) {
                // 返回缓存数据
                LOGGER.info("从redis获取到接口数据：{}，key={}", cacheDataString, cacheKey);
                return JSONObject.parseObject(cacheDataString, returnClass);
            }else {
                LOGGER.info("没有从redis获取到接口数据。开始执行接口{}逻辑...", methodFullName);
                final long start = System.currentTimeMillis();
                Object proceed = pjp.proceed();
                LOGGER.info("接口{}执行完成，耗时{}ms", methodFullName, System.currentTimeMillis() - start);
                // 写入缓存
                final String response = JSON.toJSONString(proceed);
                CacheableResponse annotation = method.getAnnotation(CacheableResponse.class);
                this.redisCache.setCacheObject(cacheKey, response, annotation.expireTime(), annotation.unit());
                LOGGER.info("已将接口{}返回数据放入redis。data={}，key={}", methodFullName, response, cacheKey);
                return proceed;
            }
        }finally {
            distributedLock.releaseLock(lockKey);
        }
    }


    private String genCacheKey(Object[] args, String methodName) {
        LOGGER.info(JSON.toJSONString(args));
        List<String> keys = Arrays.asList(this.APP_NAME, CacheableResponse.class.getSimpleName(), methodName, Md5Utils.getMD5(JSON.toJSONString(args).getBytes()));
        return StringUtils.join(keys, ":");
    }

}
