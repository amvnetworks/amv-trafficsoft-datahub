package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.util.concurrent.AbstractIdleService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Slf4j
public class TrafficsoftDeliveryPackageHandler extends AbstractIdleService {

    private volatile Subscription subscription;

    private static AtomicReferenceFieldUpdater<TrafficsoftDeliveryPackageHandler, Subscription> S =
            AtomicReferenceFieldUpdater.newUpdater(TrafficsoftDeliveryPackageHandler.class, Subscription.class, "subscription");

    private TrafficsoftDeliveryPackageHotFlux flux;

    private TrafficsoftDeliveryPackageSubscriber subscriber;

    @Builder
    TrafficsoftDeliveryPackageHandler(TrafficsoftDeliveryPackageHotFlux flux,
                                      TrafficsoftDeliveryPackageSubscriber subscriber) {
        this.flux = requireNonNull(flux);
        this.subscriber = requireNonNull(subscriber);
    }

    @Override
    protected void startUp() throws Exception {
        flux.flux()
                .publishOn(Schedulers.single())
                .subscribeOn(Schedulers.single())
                .doOnNext(delivery -> {
                    log.trace("received delivery: {}", delivery);
                })
                .doOnError(e -> log.error("", e))
                .doOnComplete(() -> {
                    log.info("ScheduledXfcdGetDataService completed.");
                })
                .doOnSubscribe(subscription -> {
                    final Subscription previousSubscriptionOrNull = S.getAndSet(this, subscription);
                    ofNullable(previousSubscriptionOrNull).ifPresent(Subscription::cancel);
                })
                .subscribe(subscriber);
    }

    @Override
    protected void shutDown() throws Exception {
        ofNullable(S.get(this))
                .ifPresent(Subscription::cancel);
    }


}
