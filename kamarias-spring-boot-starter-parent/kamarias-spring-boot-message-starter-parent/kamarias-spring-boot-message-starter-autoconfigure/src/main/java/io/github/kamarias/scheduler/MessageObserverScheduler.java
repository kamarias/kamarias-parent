package io.github.kamarias.scheduler;

import io.github.kamarias.event.MessageEvent;
import io.github.kamarias.observer.IObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2023/12/14 22:40
 */
public final class MessageObserverScheduler {


    private static final List<IObserver<MessageEvent<?>>> OBSERVERS = new ArrayList<>();

    private MessageObserverScheduler() {

    }

    public static void registerObserver(IObserver<MessageEvent<?>> observer) {
        OBSERVERS.add(observer);
    }

    public static void removeObserver(IObserver<MessageEvent<?>> observer) {
        OBSERVERS.remove(observer);
    }

    public static void noticeObserver(MessageEvent<?> event) {
        for (IObserver<MessageEvent<?>> observer : OBSERVERS) {
            observer.notify(event);
        }
    }

}
