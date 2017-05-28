package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.TopicProcessor;

import static java.util.Objects.requireNonNull;

@Slf4j
public class XfcdConfirmDeliveriesSuccessPublisher implements Publisher<ConfirmDeliveriesSuccessEvent> {

    private final TopicProcessor<ConfirmDeliveriesSuccessEvent> processor;

    public XfcdConfirmDeliveriesSuccessPublisher(EventBus eventBus) {
        this.processor = TopicProcessor.create();
        eventBus.register(this);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onNext(ConfirmDeliveriesSuccessEvent value) {
        requireNonNull(value);
        this.processor.onNext(value);
    }

    @Override
    public void subscribe(Subscriber<? super ConfirmDeliveriesSuccessEvent> subscriber) {
        requireNonNull(subscriber);
        this.processor.subscribe(subscriber);
    }
}
