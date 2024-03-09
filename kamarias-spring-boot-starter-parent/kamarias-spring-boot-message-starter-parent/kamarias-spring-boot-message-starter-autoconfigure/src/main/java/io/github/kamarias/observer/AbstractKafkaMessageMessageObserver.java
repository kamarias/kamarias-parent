package io.github.kamarias.observer;

import io.github.kamarias.event.KafkaMessageEvent;
import io.github.kamarias.scheduler.KafkaMessageObserverScheduler;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2024/3/8 22:03
 */
public abstract class AbstractKafkaMessageMessageObserver implements IObserver<KafkaMessageEvent<?, ?>> {

    public AbstractKafkaMessageMessageObserver() {
        KafkaMessageObserverScheduler.registerObserver(this);
    }

    @Override
    public final void notify(KafkaMessageEvent<?, ?> event) {
        if (check(event)) {
            handler(event);
        }
    }

}
