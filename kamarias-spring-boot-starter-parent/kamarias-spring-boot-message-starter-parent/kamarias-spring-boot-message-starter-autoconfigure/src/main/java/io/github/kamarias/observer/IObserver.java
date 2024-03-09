package io.github.kamarias.observer;

import org.springframework.context.ApplicationEvent;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2024/3/7 23:50
 */
public interface IObserver<T extends ApplicationEvent> {

    /**
     *
     * @param event
     */
    void handler(T event);

    /**
     * 是否处理通知接口
     * @return 返回是否通知
     */
    boolean check(T event);

    /**
     * @param event
     */
    void notify(T event);

}
