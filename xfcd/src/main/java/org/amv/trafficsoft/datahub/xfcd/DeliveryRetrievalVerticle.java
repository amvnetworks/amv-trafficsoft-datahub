package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmedDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * A verticle that retrieves data from the AMV TrafficSoft xfcd API
 * and publishes the response as {@link IncomingDeliveryEvent} on
 * the vertx eventbus.
 */
@Slf4j
public class DeliveryRetrievalVerticle extends AbstractVerticle {
    @Value
    public static class DeliveryRetrievalConfig {
        private static final long MIN_INTERVAL_IN_MS = TimeUnit.SECONDS.toMillis(30L);
        private static final long DEFAULT_INITIAL_DELAY_IN_MS = TimeUnit.SECONDS.toMillis(1L);
        private static final long DEFAULT_INTERVAL_IN_MS = TimeUnit.MINUTES.toMillis(1L);

        private final long maxAmountOfNodesPerDelivery;
        private final boolean refetchImmediatelyOnDeliveryWithMaxAmountOfNodes;

        private final long initialDelayInMs;
        private final long intervalInMs;

        @Builder
        public DeliveryRetrievalConfig(long maxAmountOfNodesPerDelivery,
                                       boolean refetchImmediatelyOnDeliveryWithMaxAmountOfNodes,
                                       long initialDelayInMs,
                                       long intervalInMs) {
            checkArgument(intervalInMs >= MIN_INTERVAL_IN_MS, "interval must not be lower than " + MIN_INTERVAL_IN_MS + "ms");
            checkArgument(maxAmountOfNodesPerDelivery > 0L, "max amount of nodes per delivery must be greater than zero");

            this.maxAmountOfNodesPerDelivery = maxAmountOfNodesPerDelivery;
            this.refetchImmediatelyOnDeliveryWithMaxAmountOfNodes = refetchImmediatelyOnDeliveryWithMaxAmountOfNodes;
            this.initialDelayInMs = initialDelayInMs > 0L ? initialDelayInMs : DEFAULT_INITIAL_DELAY_IN_MS;
            this.intervalInMs = intervalInMs > 0L ? intervalInMs : DEFAULT_INTERVAL_IN_MS;
        }
    }


    private final Scheduler scheduler = Schedulers.single();

    private final XfcdEvents xfcdEvents;
    private final Publisher<TrafficsoftDeliveryPackage> publisher;
    private final DeliveryRetrievalConfig config;

    private volatile long periodicTimerId;
    private volatile long initTimerId;

    private BaseSubscriber<ConfirmedDeliveryEvent> subscriber;

    public DeliveryRetrievalVerticle(XfcdEvents xfcdEvents,
                                     Publisher<TrafficsoftDeliveryPackage> publisher,
                                     DeliveryRetrievalConfig config) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
        this.publisher = requireNonNull(publisher);
        this.config = requireNonNull(config);
    }


    @Override
    public void start() throws Exception {
        if (config.isRefetchImmediatelyOnDeliveryWithMaxAmountOfNodes()) {
            this.subscriber = new BaseSubscriber<ConfirmedDeliveryEvent>() {
                @Override
                protected void hookOnNext(ConfirmedDeliveryEvent event) {
                    try {
                        onConfirmedDeliveryPackage(event);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            };

            xfcdEvents.subscribe(ConfirmedDeliveryEvent.class, this.subscriber);
        }

        this.initTimerId = vertx.setTimer(config.getInitialDelayInMs(), timerId -> {
            fetchDeliveriesAndPublishOnEventBus();

            this.periodicTimerId = vertx.setPeriodic(config.getIntervalInMs(), foo -> {
                fetchDeliveriesAndPublishOnEventBus();
            });
        });
    }

    @Override
    public void stop() throws Exception {
        vertx.cancelTimer(this.initTimerId);
        vertx.cancelTimer(this.periodicTimerId);

        scheduler.dispose();

        ofNullable(this.subscriber).ifPresent(BaseSubscriber::dispose);
    }

    private void fetchDeliveriesAndPublishOnEventBus() {
        final Flux<IncomingDeliveryEvent> events = Flux.from(publisher)
                .publishOn(scheduler)
                .subscribeOn(scheduler)
                .retry(3)
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


    private void onConfirmedDeliveryPackage(ConfirmedDeliveryEvent event) {
        TrafficsoftDeliveryPackage deliveryPackage = event.getDeliveryPackage();
        int amountOfNodes = deliveryPackage.getAmountOfNodes();
        if (amountOfNodes >= config.getMaxAmountOfNodesPerDelivery()) {
            log.info("Trigger retrieving deliveries as max amount of nodes have been found: {} >= {}",
                    amountOfNodes, config.getMaxAmountOfNodesPerDelivery());

            fetchDeliveriesAndPublishOnEventBus();
        }
    }
}
