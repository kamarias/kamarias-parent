package io.github.kamarias.event;


import io.github.kamarias.message.KafkaMessage;
import org.springframework.context.ApplicationEvent;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2024/3/7 23:53
 */
public class KafkaMessageEvent<K, V> extends ApplicationEvent {

    /**
     * kafka 消息
     */
    private final KafkaMessage<K, V> message;

    public KafkaMessageEvent(Object source, KafkaMessage<K, V> message) {
        super(source);
        this.message = message;
    }

    public KafkaMessage<K, V> getMessage() {
        return message;
    }

}
