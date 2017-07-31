package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * A verticle that retrieves data from the AMV TrafficSoft xfcd API
 * and publishes the response as {@link IncomingDeliveryEvent} on
 * the vertx eventbus.
 */
@Slf4j
public class DeliveryRetrievalVerticle extends AbstractVerticle {
    private static final long INITIAL_DELAY_IN_MS = TimeUnit.SECONDS.toMillis(1L);
    private static final long INTERVAL_IN_MS = TimeUnit.MINUTES.toMillis(1L);

    private final Scheduler scheduler = Schedulers.single();

    private final XfcdEvents xfcdEvents;
    private final Publisher<TrafficsoftDeliveryPackage> publisher;

    private final long initialDelayInMs;
    private final long intervalInMs;

    private volatile long periodicTimerId;
    private volatile long initTimerId;

    @Builder
    DeliveryRetrievalVerticle(XfcdEvents xfcdEvents,
                              Publisher<TrafficsoftDeliveryPackage> publisher,
                              long intervalInMs,
                              long initialDelayInMs) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
        this.publisher = requireNonNull(publisher);
        this.initialDelayInMs = initialDelayInMs > 0L ? initialDelayInMs : INITIAL_DELAY_IN_MS;
        this.intervalInMs = intervalInMs > 0L ? intervalInMs : INTERVAL_IN_MS;
    }

    @Override
    public void start() throws Exception {
        this.initTimerId = vertx.setTimer(initialDelayInMs, timerId -> {
            fetchDeliveriesAndPublishOnEventBus();

            DeliveryRetrievalVerticle.this.periodicTimerId = vertx.setPeriodic(intervalInMs, foo -> {
                fetchDeliveriesAndPublishOnEventBus();
            });
        });
    }

    @Override
    public void stop() throws Exception {
        vertx.cancelTimer(this.initTimerId);
        vertx.cancelTimer(this.periodicTimerId);

        scheduler.dispose();
    }

    private void fetchDeliveriesAndPublishOnEventBus() {
        final Flux<IncomingDeliveryEvent> events = Flux.from(publisher)
                .publishOn(scheduler)
                .subscribeOn(scheduler)
                .doOnError(t -> {
                    log.error("{}", t.getMessage());
                    if (log.isDebugEnabled()) {
                        log.error("", t);
                    }
                })
                .doOnNext(val -> {
                    if (log.isDebugEnabled()) {
                        log.info("Retrieved {} deliveries: {}", val.getDeliveries().size(), val.getDeliveryIds());
                    }
                })
                .map(val -> IncomingDeliveryEvent.builder()
                        .deliveryPackage(val)
                        .build());

        xfcdEvents.publish(IncomingDeliveryEvent.class, events);
    }
}