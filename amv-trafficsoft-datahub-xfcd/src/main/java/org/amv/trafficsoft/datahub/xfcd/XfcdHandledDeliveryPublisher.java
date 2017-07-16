package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.HandledDeliveryPackage;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.DirectProcessor;

import static java.util.Objects.requireNonNull;

@Slf4j
public class XfcdHandledDeliveryPublisher implements Publisher<HandledDeliveryPackage> {

    private final DirectProcessor<HandledDeliveryPackage> processor;

    public XfcdHandledDeliveryPublisher(EventBus eventBus) {
        this.processor = DirectProcessor.create();
        eventBus.register(this);
    }

    @Subscribe
    public void onNext(HandledDeliveryPackage value) {
        requireNonNull(value);

        this.processor.onNext(value);
    }

    @Override
    public void subscribe(Subscriber<? super HandledDeliveryPackage> subscriber) {
        requireNonNull(subscriber);

        this.processor.subscribe(subscriber);
    }
}
