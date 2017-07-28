package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * A verticle that listens for {@link IncomingDeliveryEvent} representing
 * an incoming delivery from AMV TrafficSoft xfcd API and stores
 * it with a {@link XfcdDataStore}. If the data store is marked as "primary"
 * a {@link ConfirmableDeliveryEvent} representing a successfully processed
 * delivery is published on the vertx eventbus.
 */
@Slf4j
public class DeliveryDataStoreVerticle extends AbstractVerticle {
    private final Scheduler scheduler = Schedulers.elastic();

    private final XfcdEvents xfcdEvents;
    private final XfcdDataStore dataStore;

    private volatile BaseSubscriber<IncomingDeliveryEvent> subscriber;

    @Builder
    DeliveryDataStoreVerticle(XfcdEvents xfcdEvents, XfcdDataStore dataStore) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
        this.dataStore = requireNonNull(dataStore);
    }

    @Override
    public void start() throws Exception {
        this.subscriber = new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent event) {
                onIncomingDeliveryPackage(event);
            }
        };

        xfcdEvents.subscribe(IncomingDeliveryEvent.class, this.subscriber);
    }

    @Override
    public void stop() throws Exception {
        ofNullable(this.subscriber).ifPresent(BaseSubscriber::dispose);
        this.scheduler.dispose();
    }

    void onIncomingDeliveryPackage(IncomingDeliveryEvent event) {
        TrafficsoftDeliveryPackage deliveryPackage = event.getDeliveryPackage();

        vertx.executeBlocking(future -> {
            persistDeliveryPackage(deliveryPackage);
            future.complete();
        }, result -> {
            if (result.failed()) {
                log.error("", result.cause());
            }

            if (result.succeeded()) {
                if (dataStore.isPrimaryDataStore()) {
                    xfcdEvents.publish(ConfirmableDeliveryEvent.class, Flux.just(ConfirmableDeliveryEvent.builder()
                            .deliveryPackage(deliveryPackage)
                            .build()));
                }
            }
        });
    }


    void persistDeliveryPackage(TrafficsoftDeliveryPackage deliveryPackage) {
        requireNonNull(deliveryPackage, "`deliveryPackage` must not be null");

        final List<DeliveryRestDto> deliveries = deliveryPackage.getDeliveries();

        if (log.isDebugEnabled()) {
            log.debug("Saving {} deliveries: {}", deliveries.size(), deliveryPackage.getDeliveryIds());
        }

        if (deliveries.isEmpty()) {
            return;
        }

        dataStore.save(deliveryPackage);

        if (log.isDebugEnabled()) {
            log.debug("Saved {} deliveries: {}", deliveries.size(), deliveryPackage.getDeliveryIds());
        }
    }
}
