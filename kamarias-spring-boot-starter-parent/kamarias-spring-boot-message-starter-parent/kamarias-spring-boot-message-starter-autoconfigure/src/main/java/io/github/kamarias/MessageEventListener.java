package io.github.kamarias;


import io.github.kamarias.event.KafkaMessageEvent;
import io.github.kamarias.event.MessageEvent;
import io.github.kamarias.scheduler.KafkaMessageObserverScheduler;
import io.github.kamarias.scheduler.MessageObserverScheduler;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2024/3/8 0:37
 */
@EnableAsync
@Component
public class MessageEventListener {

    /**
     * 本地事件监听
     * @param event 消息事件
     */
    @Async
    @EventListener
    public void messageListener(MessageEvent<?> event) {
        MessageObserverScheduler.noticeObserver(event);
    }


    /**
     * kafka消息监听
     * @param event 消息事件
     */
    @Async
    @EventListener
    public void kafkaMessageListener(KafkaMessageEvent<?, ?> event) {
        KafkaMessageObserverScheduler.noticeObserver(event);
    }


}
