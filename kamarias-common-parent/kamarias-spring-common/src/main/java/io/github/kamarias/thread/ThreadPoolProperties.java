package io.github.kamarias.thread;

import com.alibaba.fastjson.JSONObject;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2023/2/20 10:54
 */
@ConfigurationProperties(prefix = ThreadPoolProperties.PREFIX)
public class ThreadPoolProperties {

    public final static String PREFIX = "spring.task.thread.pool";

    /**
     * 核心线程池大小
     */
    private int corePoolSize = 10;

    /**
     * 最大可创建的线程数
     */
    private int maxPoolSize = 20;

    /**
     * 队列最大长度
     */
    private int queueCapacity = 100;

    /**
     * 线程池维护线程所允许的空闲时间
     */
    private int keepAliveSeconds = 300;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }


    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
