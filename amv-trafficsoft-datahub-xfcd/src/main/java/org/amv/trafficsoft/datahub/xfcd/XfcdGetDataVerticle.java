package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Slf4j
public class XfcdGetDataVerticle extends AbstractVerticle {

    private static AtomicReferenceFieldUpdater<XfcdGetDataVerticle, Subscription> S =
            AtomicReferenceFieldUpdater.newUpdater(XfcdGetDataVerticle.class, Subscription.class, "subscription");

    private volatile Subscription subscription;

    private TrafficsoftDeliveryPackageHotFlux flux;

    private TrafficsoftDeliveryPackageSubscriber subscriber;

    @Builder
    XfcdGetDataVerticle(TrafficsoftDeliveryPackageHotFlux flux,
                        TrafficsoftDeliveryPackageSubscriber subscriber) {
        this.flux = requireNonNull(flux);
        this.subscriber = requireNonNull(subscriber);
    }

    @Override
    public void start() throws Exception {
        flux.flux()
                .publishOn(Schedulers.single())
                .subscribeOn(Schedulers.single())
                .retry()
                .doOnNext(delivery -> {
                    log.info("received delivery: {}", delivery);
                })
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
    public void stop() throws Exception {
        ofNullable(S.get(this))
                .ifPresent(Subscription::cancel);
    }

}
