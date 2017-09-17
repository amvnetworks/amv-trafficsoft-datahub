package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import io.vertx.rxjava.core.AbstractVerticle;
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
        this.subscriber = new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent event) {
                try {
                    onIncomingDeliveryPackage(event);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        };

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

        vertx.executeBlocking(future -> {
            consumeIncomingDeliveryEvent(event);
            future.complete();
        }, result -> {
            if (result.failed()) {
                log.error("", result.cause());
            }

            if (result.succeeded()) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully consumed {} nodes in {}ms", deliveryPackage.getAmountOfNodes(), stopwatch
                            .elapsed(TimeUnit.MILLISECONDS));
                }
            }

            stopwatch.stop();
        });
    }

    @VisibleForTesting
    void consumeIncomingDeliveryEvent(IncomingDeliveryEvent event) {
        requireNonNull(event, "`event` must not be null");

        TrafficsoftDeliveryPackage deliveryPackage = event.getDeliveryPackage();

        if (deliveryPackage.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Discard empty deliveries.", deliveryPackage.getContractId());
            }
            return;
        }

        final List<DeliveryRestDto> deliveries = deliveryPackage.getDeliveries();
        if (log.isDebugEnabled()) {
            log.debug("Consuming {} deliveries: {}", deliveries.size(), deliveryPackage.getDeliveryIds());
        }

        incomingDeliveryEventConsumer.accept(event, (consumer) -> {
            if (log.isDebugEnabled()) {
                log.debug("Confirming {} deliveries: {}", deliveries.size(), deliveryPackage.getDeliveryIds());
            }

            xfcdEvents.publish(ConfirmableDeliveryEvent.class, Flux.just(ConfirmableDeliveryEvent.builder()
                    .deliveryPackage(deliveryPackage)
                    .build()));
        });

        if (log.isDebugEnabled()) {
            log.debug("Consumed {} deliveries: {}", deliveries.size(), deliveryPackage.getDeliveryIds());
        }
    }
}
