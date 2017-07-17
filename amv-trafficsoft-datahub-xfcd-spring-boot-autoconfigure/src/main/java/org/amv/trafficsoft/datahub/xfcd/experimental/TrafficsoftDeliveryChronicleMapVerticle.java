package org.amv.trafficsoft.datahub.xfcd.experimental;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.map.ChronicleMap;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.datahub.xfcd.event.VertxEvents;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Slf4j
public class TrafficsoftDeliveryChronicleMapVerticle extends AbstractVerticle {
    private final Scheduler scheduler = Schedulers.single();

    private final ChronicleMap<Long, DeliveryRestDto> deliveryDatabase;

    private volatile MessageConsumer<String> consumer;

    @Builder
    TrafficsoftDeliveryChronicleMapVerticle(ChronicleMap<Long, DeliveryRestDto> deliveryDatabase) {
        this.deliveryDatabase = requireNonNull(deliveryDatabase);
    }

    @Override
    public void start() throws Exception {
        this.consumer = vertx.eventBus().consumer(VertxEvents.deliveryPackage, new Handler<Message<String>>() {
            public void handle(Message<String> objectMessage) {
                final TrafficsoftDeliveryPackageImpl trafficsoftDeliveryPackage = Json.decodeValue(objectMessage.body(), TrafficsoftDeliveryPackageImpl.class);

                Flux.just(trafficsoftDeliveryPackage)
                        .subscribeOn(scheduler)
                        .subscribe(next -> {
                            onNext(trafficsoftDeliveryPackage);
                        });
            }
        });
    }

    @Override
    public void stop() throws Exception {
        ofNullable(consumer).ifPresent(MessageConsumer::unregister);
        scheduler.dispose();
    }

    protected void onNext(TrafficsoftDeliveryPackage deliveryPackage) {
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
