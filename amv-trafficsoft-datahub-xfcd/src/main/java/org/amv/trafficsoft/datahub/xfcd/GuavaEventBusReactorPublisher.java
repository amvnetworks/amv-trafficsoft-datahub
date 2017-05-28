package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.TopicProcessor;

import static java.util.Objects.requireNonNull;

@Slf4j
public class GuavaEventBusReactorPublisher<T> implements Publisher<T> {

    private final TopicProcessor<T> processor;

    public GuavaEventBusReactorPublisher(EventBus eventBus) {
        this.processor = TopicProcessor.create();
        eventBus.register(this);
    }

    @Subscribe
    public void onNext(T value) {
        requireNonNull(value);

        //log.info("Got an event of type {}", value.getClass());
        this.processor.onNext(value);
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        requireNonNull(subscriber);

        log.info("Subscribing {}", subscriber.getClass());
        this.processor.subscribe(subscriber);
    }
}
