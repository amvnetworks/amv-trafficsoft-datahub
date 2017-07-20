package org.amv.trafficsoft.datahub.xfcd.jdbc;

import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.XfcdEvents;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryPackageJdbcDao;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Slf4j
public class TrafficsoftDeliveryJdbcVerticle extends AbstractVerticle {
    private final Scheduler scheduler = Schedulers.elastic();

    private final XfcdEvents xfcdEvents;
    private final TrafficsoftDeliveryPackageJdbcDao deliveryPackageDao;
    private final boolean primaryDataStore;

    private volatile BaseSubscriber<IncomingDeliveryEvent> eventSubscriber;

    @Builder
    TrafficsoftDeliveryJdbcVerticle(XfcdEvents xfcdEvents, TrafficsoftDeliveryPackageJdbcDao deliveryPackageDao, boolean primaryDataStore) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
        this.deliveryPackageDao = requireNonNull(deliveryPackageDao);
        this.primaryDataStore = primaryDataStore;
    }

    @Override
    public void start() throws Exception {
        this.eventSubscriber = new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent event) {
                onIncomingDeliveryPackage(event);

                if (primaryDataStore) {
                    xfcdEvents.publish(ConfirmableDeliveryEvent.class, Flux.just(ConfirmableDeliveryEvent.builder()
                            .deliveryPackage(event.getDeliveryPackage())
                            .build()));
                }
            }

            @Override
            protected void hookOnCancel() {
                log.info("Cancelled subscription in {}", this.getClass().getName());
            }
        };

        xfcdEvents.subscribe(IncomingDeliveryEvent.class, eventSubscriber);
    }

    @Override
    public void stop() throws Exception {
        ofNullable(eventSubscriber).ifPresent(BaseSubscriber::dispose);
        scheduler.dispose();
    }

    void onIncomingDeliveryPackage(IncomingDeliveryEvent incomingDeliveryEvent) {
        requireNonNull(incomingDeliveryEvent, "`deliveryPackage` must not be null");

        final TrafficsoftDeliveryPackage deliveryPackage = incomingDeliveryEvent
                .getDeliveryPackage();

        final List<DeliveryRestDto> deliveries = deliveryPackage.getDeliveries();

        if (log.isDebugEnabled()) {
            log.debug("saving {} deliveries", deliveries.size());
        }

        if (deliveries.isEmpty()) {
            return;
        }

        deliveryPackageDao.save(deliveryPackage);
    }
}
