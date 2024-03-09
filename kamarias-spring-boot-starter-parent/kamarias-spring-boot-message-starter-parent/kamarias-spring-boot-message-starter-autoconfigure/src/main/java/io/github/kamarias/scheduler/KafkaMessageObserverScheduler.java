package io.github.kamarias.scheduler;

import io.github.kamarias.event.KafkaMessageEvent;
import io.github.kamarias.observer.IObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2023/12/14 22:40
 */
public final class KafkaMessageObserverScheduler {


    private static final List<IObserver<KafkaMessageEvent<?,?>>> OBSERVERS = new ArrayList<>();

    private KafkaMessageObserverScheduler() {

    }

    public static void registerObserver(IObserver<KafkaMessageEvent<?,?>> observer) {
        OBSERVERS.add(observer);
    }

    public static void removeObserver(IObserver<KafkaMessageEvent<?,?>> observer) {
        OBSERVERS.remove(observer);
    }

    public static void noticeObserver(KafkaMessageEvent<?,?> event) {
        for (IObserver<KafkaMessageEvent<?, ?>> observer : OBSERVERS) {
            observer.notify(event);
        }
    }
}
