package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import io.vertx.rxjava3.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * A verticle that listens for {@link IncomingDeliveryEvent} representing
 * an incoming delivery from AMV TrafficSoft xfcd API and consumes
 * it with a {@link DeliveryConsumer}. If the data store is marked as "primary"
 * a {@link ConfirmableDeliveryEvent} representing a successfully processed
 * delivery is published on the vertx eventbus.
 */
@Slf4j
public class IncomingDeliveryConsumerVerticle extends AbstractVerticle {
    private final XfcdEvents xfcdEvents;
    private final IncomingDeliveryEventConsumer incomingDeliveryEventConsumer;

    private volatile BaseSubscriber<IncomingDeliveryEvent> subscriber;

    @Builder
    IncomingDeliveryConsumerVerticle(XfcdEvents xfcdEvents, IncomingDeliveryEventConsumer incomingDeliveryEventConsumer) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
        this.incomingDeliveryEventConsumer = requireNonNull(incomingDeliveryEventConsumer);
    }

    @Override
    public void start() throws Exception {
        this.subscriber = new BaseSubscriber<>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent event) {
                try {
                    onIncomingDeliveryPackage(event);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        };

        log.info("Subscribing to {} on Vert.x event bus", IncomingDeliveryEvent.class.getSimpleName());
        xfcdEvents.subscribe(IncomingDeliveryEvent.class, this.subscriber);
    }

    @Override
    public void stop() throws Exception {
        ofNullable(this.subscriber).ifPresent(BaseSubscriber::dispose);
    }

    private void onIncomingDeliveryPackage(IncomingDeliveryEvent event) {
        requireNonNull(event, "`event` must not be null");

        TrafficsoftDeliveryPackage deliveryPackage = event.getDeliveryPackage();
        final Stopwatch stopwatch = Stopwatch.createStarted();

        if (log.isDebugEnabled()) {
            log.debug("Start consuming delivery: contract={}, deliveries={}, nodes={}, ids={}",
                    deliveryPackage.getContractId(),
                    deliveryPackage.getDeliveries().size(),
                    deliveryPackage.getAmountOfNodes(),
                    deliveryPackage.getDeliveryIds());
        }

        vertx.<Void>executeBlocking(promise -> {
            try {
                consumeIncomingDeliveryEvent(event);
                promise.complete(null);
            } catch (Exception e) {
                promise.fail(e);
            }
        })
        .doOnError(t -> {
            log.error("Failed to consume delivery for contract {} (deliveries={}, nodes={}, ids={}): {}",
                    deliveryPackage.getContractId(),
                    deliveryPackage.getDeliveries().size(),
                    deliveryPackage.getAmountOfNodes(),
                    deliveryPackage.getDeliveryIds(),
                    t.getMessage(), t);
        })
        .doOnSuccess(ignored -> {
            if (log.isDebugEnabled()) {
                log.debug("Successfully consumed {} nodes in {}ms", deliveryPackage.getAmountOfNodes(), stopwatch
                        .elapsed(TimeUnit.MILLISECONDS));
            }
        })
        .doFinally(() -> stopwatch.stop())
        .subscribe();
    }

    @VisibleForTesting
    void consumeIncomingDeliveryEvent(IncomingDeliveryEvent event) {
        requireNonNull(event, "`event` must not be null");

        TrafficsoftDeliveryPackage deliveryPackage = event.getDeliveryPackage();

        if (deliveryPackage.isEmpty()) {
            log.info("Not consuming delivery for contract {} because it is empty (0 deliveries, 0 nodes)",
                    deliveryPackage.getContractId());
            return;
        }

        final List<DeliveryRestDto> deliveries = deliveryPackage.getDeliveries();
        if (log.isDebugEnabled()) {
            log.debug("Consuming {} deliveries: {}", deliveries.size(), deliveryPackage.getDeliveryIds());
        }

        try {
            incomingDeliveryEventConsumer.accept(event, (consumer) -> {
                if (log.isDebugEnabled()) {
                    log.debug("Confirming {} deliveries: {}", deliveries.size(), deliveryPackage.getDeliveryIds());
                }

                xfcdEvents.publish(ConfirmableDeliveryEvent.class, Flux.just(ConfirmableDeliveryEvent.builder()
                        .deliveryPackage(deliveryPackage)
                        .build()));
            });
        } catch (Exception e) {
            log.error("Exception while consuming delivery for contract {} (deliveries={}, nodes={}, ids={}): {}",
                    deliveryPackage.getContractId(),
                    deliveries.size(),
                    deliveryPackage.getAmountOfNodes(),
                    deliveryPackage.getDeliveryIds(),
                    e.getMessage(), e);
            throw e;
        }

        if (log.isDebugEnabled()) {
            log.debug("Consumed {} deliveries: {}", deliveries.size(), deliveryPackage.getDeliveryIds());
        }
    }
}
