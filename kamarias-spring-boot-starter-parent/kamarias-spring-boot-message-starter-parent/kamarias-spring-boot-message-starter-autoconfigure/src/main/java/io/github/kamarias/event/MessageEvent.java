package io.github.kamarias.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2024/3/8 21:50
 */
public class MessageEvent<T> extends ApplicationEvent {

    /**
     * 本地消息
     */
    private final T message;

    public MessageEvent(Object source, T message) {
        super(source);
        this.message = message;
    }

    public T getMessage() {
        return message;
    }

}
