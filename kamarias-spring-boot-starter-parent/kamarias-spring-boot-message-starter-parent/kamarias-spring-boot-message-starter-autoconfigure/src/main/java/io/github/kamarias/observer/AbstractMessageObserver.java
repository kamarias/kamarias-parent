package io.github.kamarias.observer;

import io.github.kamarias.event.MessageEvent;
import io.github.kamarias.scheduler.MessageObserverScheduler;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2024/3/8 0:22
 */
public abstract class AbstractMessageObserver implements IObserver<MessageEvent<?>> {

    public AbstractMessageObserver() {
        MessageObserverScheduler.registerObserver(this);
    }

    @Override
    public final void notify(MessageEvent<?> event) {
        if (check(event)) {
            handler(event);
        }
    }

}
