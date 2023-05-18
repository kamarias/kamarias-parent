package io.github.kamarias;


import io.github.kamarias.lock.DistributedLock;
import io.github.kamarias.lock.RedisDistributedLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 分布式锁自动配置默认使用Redis作为分布式锁
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/4 9:16
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(DistributedLock.class)
public class DistributedLockAutoConfiguration {

    /**
     * 默认使用
     * 使用redis作为分布式锁 使用前需要保证已经注入 redisTemplate 的 bean 实例
     */
    @Bean
    @ConditionalOnMissingBean(DistributedLock.class)
    @ConditionalOnClass(RedisDistributedLock.class)
    public DistributedLock redisDistributedLock(StringRedisTemplate redisTemplate) {
        return new RedisDistributedLock(redisTemplate);
    }



}
