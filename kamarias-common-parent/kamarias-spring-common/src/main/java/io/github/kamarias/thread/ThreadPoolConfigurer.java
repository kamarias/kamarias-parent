package io.github.kamarias.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 * @author wangyuxing@gogpay.cn
 * @date 2023/2/20 10:51
 */

@EnableAsync
@Configuration(proxyBeanMethods = false)
@Import(ThreadPoolProperties.class)
public class ThreadPoolConfigurer {

    private final ThreadPoolProperties threadPoolProperties;

    public ThreadPoolConfigurer(ThreadPoolProperties threadPoolProperties) {
        this.threadPoolProperties = threadPoolProperties;
    }

    @Bean(name = "taskThreadExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
        executor.setCorePoolSize(threadPoolProperties.getCorePoolSize());
        executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
        executor.setThreadNamePrefix("application-task-thread-");
        // 线程池对拒绝任务(无线程可用)的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

}
