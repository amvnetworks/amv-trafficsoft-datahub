package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.collect.ImmutableList;
import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * A verticle that retrieves data from the AMV TrafficSoft xfcd API
 * and publishes the response as {@link IncomingDeliveryEvent} on
 * the vertx eventbus.
 */
@Slf4j
public class DeliveryRetrievalVerticle extends AbstractVerticle {
    private static final long DEFAULT_INITIAL_DELAY_IN_MS = TimeUnit.SECONDS.toMillis(1L);
    private static final long DEFAULT_INTERVAL_IN_MS = TimeUnit.MINUTES.toMillis(1L);
    private static final int DEFAULT_MAX_AMOUNT_OF_NODES_PER_DELIVERY = 5_000;

    private final Scheduler scheduler = Schedulers.single();

    private final XfcdEvents xfcdEvents;
    private final Publisher<TrafficsoftDeliveryPackage> publisher;

    private final long initialDelayInMs;
    private final long intervalInMs;
    private final int maxAmountOfNodesPerDelivery;

    private volatile long periodicTimerId;
    private volatile long initTimerId;

    @Builder
    DeliveryRetrievalVerticle(XfcdEvents xfcdEvents,
                              Publisher<TrafficsoftDeliveryPackage> publisher,
                              long intervalInMs,
                              long initialDelayInMs,
                              int maxAmountOfNodesPerDelivery) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
        this.publisher = requireNonNull(publisher);
        this.initialDelayInMs = initialDelayInMs > 0L ? initialDelayInMs : DEFAULT_INITIAL_DELAY_IN_MS;
        this.intervalInMs = intervalInMs > 0L ? intervalInMs : DEFAULT_INTERVAL_IN_MS;
        this.maxAmountOfNodesPerDelivery = maxAmountOfNodesPerDelivery > 0 ? maxAmountOfNodesPerDelivery : DEFAULT_MAX_AMOUNT_OF_NODES_PER_DELIVERY;
    }

    @Override
    public void start() throws Exception {
        this.initTimerId = vertx.setTimer(initialDelayInMs, timerId -> {
            fetchDeliveriesAndPublishOnEventBus();

            this.periodicTimerId = vertx.setPeriodic(intervalInMs, foo -> {
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
        final Flux<IncomingDeliveryEvent> events = fetchDeliveriesRecursively()
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
                        log.debug("Retrieved {} deliveries with {} nodes: {}", val.getDeliveries().size(),
                                val.getAmountOfNodes(),
                                val.getDeliveryIds());
                    }
                })
                .map(val -> IncomingDeliveryEvent.builder()
                        .deliveryPackage(val)
                        .build());

        xfcdEvents.publish(IncomingDeliveryEvent.class, events);
    }

    private Flux<TrafficsoftDeliveryPackage> fetchDeliveriesRecursively() {
        Callable<List<TrafficsoftDeliveryPackage>> fetchDeliveriesRecursivelyCallable = () -> {
            ImmutableList.Builder<TrafficsoftDeliveryPackage> listBuilder = ImmutableList.builder();

            Optional<TrafficsoftDeliveryPackage> deliveryPackageOptional = Optional.of(Mono.from(publisher))
                    .map(Mono::block);

            if (deliveryPackageOptional.isPresent()) {
                TrafficsoftDeliveryPackage deliveryPackage = deliveryPackageOptional.get();

                listBuilder.add(deliveryPackage);

                int amountOfNodes = deliveryPackage.getAmountOfNodes();
                boolean fetchMoreDeliveries = amountOfNodes >= maxAmountOfNodesPerDelivery;
                if (fetchMoreDeliveries) {
                    listBuilder.addAll(fetchDeliveriesRecursively().toIterable());
                }
            }

            return listBuilder.build();
        };

        return Mono.fromCallable(fetchDeliveriesRecursivelyCallable)
                .flux()
                .flatMap(Flux::fromIterable);
    }
}
