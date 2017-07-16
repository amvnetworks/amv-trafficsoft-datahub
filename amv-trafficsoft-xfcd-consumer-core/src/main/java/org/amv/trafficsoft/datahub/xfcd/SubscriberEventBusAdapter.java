package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SubscriberEventBusAdapter<T> {

    private final Supplier<Subscriber<T>> subscriberSupplier;

    public SubscriberEventBusAdapter(Supplier<Subscriber<T>> subscriberSupplier, EventBus eventBus) {
        this.subscriberSupplier = requireNonNull(subscriberSupplier);
        eventBus.register(this);
    }

    @Subscribe
    public void onNext(T value) {
        Optional.ofNullable(subscriberSupplier.get())
                .ifPresent(subscriber -> Mono.just(value)
                        .subscribe(subscriber));
    }
}
