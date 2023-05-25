package io.github.kamarias.lock;

import io.github.kamarias.lock.AbstractDistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 分布式Redis锁具体实现
 *
 * @author wangyuxing
 */
public class RedisDistributedLock extends AbstractDistributedLock {

    private final Logger LOGGER = LoggerFactory.getLogger(RedisDistributedLock.class);

    private final StringRedisTemplate redisTemplate;

    private final ThreadLocal<Map<String, String>> context = new ThreadLocal<Map<String, String>>() {
        @Override
        public Map<String, String> get() {
            Map<String, String> lockMap = super.get();
            if (lockMap == null) {
                lockMap = new HashMap<>(16);
                this.set(lockMap);
            }
            return lockMap;
        }
    };

    /**
     * 加锁lua脚本
     */
    private static final String LOCK_LUA;

    /**
     * 解锁lua脚本
     */
    private static final String UNLOCK_LUA;

    /**
     * 续期lua脚本
     */
    private static final String DELAY_TIME_LUA;

    /**
     * 加锁lua脚本对象
     */
    private final DefaultRedisScript<Long> LOCK_LUA_SCRIPT = new DefaultRedisScript<>(LOCK_LUA, Long.class);

    /**
     * 解锁lua脚本对象
     */
    private final DefaultRedisScript<Long> UNLOCK_LUA_SCRIPT = new DefaultRedisScript<>(UNLOCK_LUA, Long.class);

    /**
     * 续期lua脚本对象
     */
    private final RedisScript<Long> DELAY_TIME_LUA_SCRIPT = new DefaultRedisScript<>(DELAY_TIME_LUA, Long.class);

    static {
        // 加锁成功返回 1，否则返回剩余过期时间
        LOCK_LUA = "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 " +
                "then " +
                "return redis.call('pexpire', KEYS[1], tonumber(ARGV[2])) " +
                "else " +
                "return redis.call('pttl', KEYS[1])" +
                "end";
        UNLOCK_LUA = "if redis.call(\"get\",KEYS[1]) == ARGV[1] " +
                "then " +
                "    return redis.call(\"del\",KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end ";
        DELAY_TIME_LUA = "if redis.call(\"get\",KEYS[1]) == ARGV[1] " +
                "then " +
                "return redis.call(\"pexpire\", KEYS[1], tonumber(ARGV[2]))" +
                "else" +
                "  return 0 " +
                "end";
    }

    public RedisDistributedLock(StringRedisTemplate redisTemplate) {
        super();
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean lock(String key, long expire, int retryTimes, long sleepMillis) {
        boolean result = setRedis(key, expire);
        // 如果获取锁失败,按照传入的重试次数进行重试
        while ((!result) && retryTimes-- > 0) {
            try {
                LOGGER.debug("lock failed, retrying..." + retryTimes);
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                LOGGER.warn("thread={} is interrupt", Thread.currentThread().getName());
                Thread.currentThread().interrupt();
                return false;
            }
            result = setRedis(key, expire);
        }
        return result;
    }

    private boolean setRedis(String key, long expire) {
        try {
            String uuid = UUID.randomUUID().toString();
            Map<String, String> lockMap = context.get();
            lockMap.put(key, uuid);
            Long result = this.redisTemplate.execute(LOCK_LUA_SCRIPT,
                    Collections.singletonList(key),
                    uuid,
                    String.valueOf(expire == -1L ? 30000 : expire));
            // 等于 1 才能算加锁成功
            boolean res = result != null && result.equals(1L);
            if (res && expire == -1L) {
                // 自动延时
                delayLockTime(key, uuid, 30000);
            }
            return res;
        } catch (Exception e) {
            LOGGER.error("set redis occurred an exception", e);
        }
        return false;
    }

    @Override
    public boolean releaseLock(String key) {
        // 释放锁的时候,有可能因为持锁之后方法执行时间大于锁的有效期,此时有可能已经被另外一个线程持有锁,所以不能直接删除
        try {
            Map<String, String> lockMap = context.get();
            Long result = this.redisTemplate.execute(UNLOCK_LUA_SCRIPT, Collections.singletonList(key), lockMap.get(key));
            return result != null && result > 0;
        } catch (Exception e) {
            LOGGER.error("release lock occurred an exception", e);
        } finally {
            // 移除线程变量
            context.remove();
        }
        return false;
    }

    /**
     * 续期锁方法
     *
     * @param key    锁的名字
     * @param value  锁的值
     * @param expire 过期时间
     */
    private void delayLockTime(final String key, final String value, final long expire) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Long result = redisTemplate.execute(DELAY_TIME_LUA_SCRIPT,
                        Collections.singletonList(key),
                        value, String.valueOf(expire));
                LOGGER.info("update lock time, lock name: {}", key);
                if (result != null && result.equals(1L)) {
                    // 等于1 监听下一次续期
                    delayLockTime(key, value, expire);
                }
            }
        }, expire / 3);
    }

}
