package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * A verticle that listens for {@link IncomingDeliveryEvent} representing
 * an incoming delivery from AMV TrafficSoft xfcd API and stores
 * it with a {@link XfcdDataConsumer}. If the data store is marked as "primary"
 * a {@link ConfirmableDeliveryEvent} representing a successfully processed
 * delivery is published on the vertx eventbus.
 */
@Slf4j
public class XfcdDataConsumerVerticle extends AbstractVerticle {
    private final XfcdEvents xfcdEvents;
    private final XfcdDataConsumer xfcdDataConsumer;

    private volatile BaseSubscriber<IncomingDeliveryEvent> subscriber;

    @Builder
    XfcdDataConsumerVerticle(XfcdEvents xfcdEvents, XfcdDataConsumer xfcdDataConsumer) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
        this.xfcdDataConsumer = requireNonNull(xfcdDataConsumer);
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

    void onIncomingDeliveryPackage(IncomingDeliveryEvent event) {
        TrafficsoftDeliveryPackage deliveryPackage = event.getDeliveryPackage();

        vertx.executeBlocking(future -> {
            consumeDeliveryPackage(deliveryPackage);
            future.complete();
        }, result -> {
            if (result.failed()) {
                log.error("", result.cause());
            }

            if (result.succeeded()) {
                if (xfcdDataConsumer.sendsConfirmationEvents()) {
                    xfcdEvents.publish(ConfirmableDeliveryEvent.class, Flux.just(ConfirmableDeliveryEvent.builder()
                            .deliveryPackage(deliveryPackage)
                            .build()));
                }
            }
        });
    }


    void consumeDeliveryPackage(TrafficsoftDeliveryPackage deliveryPackage) {
        requireNonNull(deliveryPackage, "`deliveryPackage` must not be null");

        final List<DeliveryRestDto> deliveries = deliveryPackage.getDeliveries();

        if (log.isDebugEnabled()) {
            log.debug("Saving {} deliveries: {}", deliveries.size(), deliveryPackage.getDeliveryIds());
        }

        if (deliveries.isEmpty()) {
            return;
        }

        xfcdDataConsumer.consume(deliveryPackage);

        if (log.isDebugEnabled()) {
            log.debug("Saved {} deliveries: {}", deliveries.size(), deliveryPackage.getDeliveryIds());
        }
    }
}
