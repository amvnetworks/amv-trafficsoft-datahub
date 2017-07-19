package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.Json;
import io.vertx.core.streams.Pump;
import io.vertx.ext.reactivestreams.ReactiveReadStream;
import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.VertxEvents;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Slf4j
public class XfcdGetDataVerticle extends AbstractVerticle {
    private static final long INITIAL_DELAY_IN_MS = TimeUnit.SECONDS.toMillis(1L);
    private static final long INTERVAL_IN_MS = TimeUnit.MINUTES.toMillis(1L);

    private static final Scheduler scheduler = Schedulers.single();

    private Publisher<TrafficsoftDeliveryPackage> publisher;

    private long initialDelayInMs = TimeUnit.SECONDS.toMillis(1L);
    private long intervalInMs = TimeUnit.MINUTES.toSeconds(1L);

    private volatile long periodicTimerId;
    private volatile long initTimerId;

    @Builder
    XfcdGetDataVerticle(Publisher<TrafficsoftDeliveryPackage> publisher,
                        long intervalInMs,
                        long initialDelayInMs) {

        this.publisher = requireNonNull(publisher);
        this.initialDelayInMs = initialDelayInMs > 0L ? initialDelayInMs : INITIAL_DELAY_IN_MS;
        this.intervalInMs = intervalInMs > 0L ? intervalInMs : INTERVAL_IN_MS;
    }

    @Override
    public void start() throws Exception {
        this.initTimerId = vertx.setTimer(initialDelayInMs, timerId -> {
            fetchDeliveriesAndPublishOnEventBus();

            XfcdGetDataVerticle.this.periodicTimerId = vertx.setPeriodic(intervalInMs, foo -> {
                fetchDeliveriesAndPublishOnEventBus();
            });
        });

    }


    @Override
    public void stop() throws Exception {
        vertx.cancelTimer(this.initTimerId);
        vertx.cancelTimer(this.periodicTimerId);
    }

    private void fetchDeliveriesAndPublishOnEventBus() {
        ReactiveReadStream<Object> rrs = ReactiveReadStream.readStream();

        Flux.from(publisher)
                .publishOn(scheduler)
                .subscribeOn(scheduler)
                .doOnError(t -> {
                    log.error("{}", t.getMessage());
                    if (log.isDebugEnabled()) {
                        log.error("", t);
                    }
                })
                .map(Json::encode)
                .subscribe(rrs);

        MessageProducer<Object> messageProducer = vertx.getDelegate()
                .eventBus().publisher(VertxEvents.deliveryPackage);

        Pump pump = Pump.pump(rrs, messageProducer);

        pump.start();
    }
}
