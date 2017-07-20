package org.amv.trafficsoft.datahub.xfcd.experimental;

import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.map.ChronicleMap;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.XfcdEvents;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class TrafficsoftDeliveryChronicleMapVerticle extends AbstractVerticle {
    private final Scheduler scheduler = Schedulers.single();

    private final ChronicleMap<Long, DeliveryRestDto> deliveryDatabase;
    private final XfcdEvents xfcdEvents;

    private BaseSubscriber<IncomingDeliveryEvent> subscriber = new BaseSubscriber<IncomingDeliveryEvent>() {
        @Override
        protected void hookOnNext(IncomingDeliveryEvent value) {
            onIncomingDeliveryEvent(value);
        }
    };

    @Builder
    TrafficsoftDeliveryChronicleMapVerticle(XfcdEvents xfcdEvents, ChronicleMap<Long, DeliveryRestDto> deliveryDatabase) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
        this.deliveryDatabase = requireNonNull(deliveryDatabase);
    }

    @Override
    public void start() throws Exception {
        xfcdEvents.subscribe(IncomingDeliveryEvent.class, subscriber);
    }

    @Override
    public void stop() throws Exception {
        subscriber.dispose();
        scheduler.dispose();
    }

    void onIncomingDeliveryEvent(IncomingDeliveryEvent event) {
        requireNonNull(event, "`event` must not be null");

        final TrafficsoftDeliveryPackage deliveryPackage = event.getDeliveryPackage();
        requireNonNull(deliveryPackage, "`deliveryPackage` must not be null");

        final List<DeliveryRestDto> deliveries = deliveryPackage.getDeliveries();

        if (log.isDebugEnabled()) {
            log.debug("saving {} deliveries", deliveries.size());
        }

        if (deliveries.isEmpty()) {
            return;
        }

        final Map<Long, DeliveryRestDto> deliveriesById = deliveries.stream()
                .collect(Collectors.toMap(DeliveryRestDto::getDeliveryId, Function.identity()));

        deliveryDatabase.putAll(deliveriesById);
    }
}
