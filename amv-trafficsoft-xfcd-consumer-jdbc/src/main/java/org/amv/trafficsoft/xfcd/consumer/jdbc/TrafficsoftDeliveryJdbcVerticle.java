package org.amv.trafficsoft.xfcd.consumer.jdbc;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.ConsumedTrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.datahub.xfcd.VertxMessages;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Slf4j
public class TrafficsoftDeliveryJdbcVerticle extends AbstractVerticle {
    private final TrafficsoftDeliveryJdbcDao deliveryDao;

    private volatile MessageConsumer<String> consumer;

    @Builder
    TrafficsoftDeliveryJdbcVerticle(TrafficsoftDeliveryJdbcDao deliveryDao) {
        this.deliveryDao = requireNonNull(deliveryDao);
    }

    @Override
    public void start() throws Exception {
        this.consumer = vertx.eventBus().consumer(VertxMessages.deliveryPackage, new Handler<Message<String>>() {
            public void handle(Message<String> objectMessage) {
                final TrafficsoftDeliveryPackageImpl trafficsoftDeliveryPackage = Json.decodeValue(objectMessage.body(), TrafficsoftDeliveryPackageImpl.class);

                Flux.just(trafficsoftDeliveryPackage)
                        .subscribeOn(Schedulers.single())
                        .subscribe(next -> {
                            onNext(trafficsoftDeliveryPackage);

                            vertx.eventBus().publish(VertxMessages.deliveryPackageConsumed, Json.encode(ConsumedTrafficsoftDeliveryPackage.builder()
                                    .delivery(trafficsoftDeliveryPackage)
                                    .build()));
                        });
            }
        });
    }

    @Override
    public void stop() throws Exception {
        ofNullable(consumer).ifPresent(MessageConsumer::unregister);
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

        final List<TrafficsoftDeliveryEntity> deliveryEntities = deliveries.stream()
                .map(val -> TrafficsoftDeliveryEntity.builder()
                        .id(val.getDeliveryId())
                        .timestamp(val.getTimestamp().toInstant())
                        .confirmedAt(null)
                        .build())
                .collect(toList());

        deliveryDao.saveAll(deliveryEntities);
    }
}
