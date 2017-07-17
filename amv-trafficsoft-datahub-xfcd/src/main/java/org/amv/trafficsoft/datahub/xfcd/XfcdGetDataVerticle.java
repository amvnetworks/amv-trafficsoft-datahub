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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public class XfcdGetDataVerticle extends AbstractVerticle {
    private static final Scheduler scheduler = Schedulers.single();

    private Publisher<TrafficsoftDeliveryPackage> publisher;

    private final long intervalInSeconds;

    private long periodicTimerId;
    private long initTimerId;

    @Builder
    XfcdGetDataVerticle(Publisher<TrafficsoftDeliveryPackage> publisher,
                        long intervalInSeconds) {
        checkArgument(intervalInSeconds > 0L);
        this.publisher = requireNonNull(publisher);
        this.intervalInSeconds = intervalInSeconds;
    }

    @Override
    public void start() throws Exception {
        final long intervalInMilliseconds = TimeUnit.SECONDS.toMillis(this.intervalInSeconds);

        this.initTimerId = vertx.setTimer(TimeUnit.SECONDS.toMillis(1), timerId -> {
            fetchDeliveriesAndPublishOnEventBus();

            XfcdGetDataVerticle.this.periodicTimerId = vertx.setPeriodic(intervalInMilliseconds, foo -> {
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
