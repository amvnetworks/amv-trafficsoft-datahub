package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.DirectProcessor;

import static java.util.Objects.requireNonNull;

@Slf4j
public class XfcdHandledDeliveryPublisher implements Publisher<HandledDelivery> {

    private final DirectProcessor<HandledDelivery> processor;

    public XfcdHandledDeliveryPublisher(EventBus eventBus) {
        this.processor = DirectProcessor.create();
        eventBus.register(this);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onNext(HandledDelivery value) {
        requireNonNull(value);
        this.processor.onNext(value);
    }

    @Override
    public void subscribe(Subscriber<? super HandledDelivery> subscriber) {
        requireNonNull(subscriber);

        log.info("Subscribing {}", subscriber.getClass());
        this.processor.subscribe(subscriber);
    }
}
