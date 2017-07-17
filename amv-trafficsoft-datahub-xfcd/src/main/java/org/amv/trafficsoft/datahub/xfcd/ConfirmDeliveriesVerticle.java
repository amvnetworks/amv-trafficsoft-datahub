package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.collect.ImmutableList;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmedDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.event.VertxEvents;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Slf4j
public class ConfirmDeliveriesVerticle extends AbstractVerticle {
    private final XfcdClient xfcdClient;
    private final long contractId;

    private volatile MessageConsumer<String> consumer;

    @Builder
    ConfirmDeliveriesVerticle(XfcdClient xfcdClient,
                              long contractId) {
        this.xfcdClient = requireNonNull(xfcdClient);
        this.contractId = contractId;
    }

    @Override
    public void start() throws Exception {
        this.consumer = vertx.eventBus().consumer(VertxEvents.deliveryPackageInternallyConfirmed, new Handler<Message<String>>() {
            public void handle(Message<String> objectMessage) {
                final ConfirmableDeliveryPackage confirmableDeliveryPackage = Json.decodeValue(objectMessage.body(), ConfirmableDeliveryPackage.class);
                confirm(confirmableDeliveryPackage);
            }
        });
    }

    @Override
    public void stop() throws Exception {
        ofNullable(consumer).ifPresent(MessageConsumer::unregister);
    }


    private void confirm(ConfirmableDeliveryPackage confirmableDeliveryPackage) {
        Set<Long> deliveryIds = Stream.of(confirmableDeliveryPackage)
                .map(ConfirmableDeliveryPackage::getDeliveryPackage)
                .map(TrafficsoftDeliveryPackage::getDeliveries)
                .flatMap(Collection::stream)
                .map(DeliveryRestDto::getDeliveryId)
                .collect(Collectors.toSet());

        xfcdClient.confirmDeliveries(contractId, ImmutableList.copyOf(deliveryIds))
                .toObservable()
                .map(foo -> confirmableDeliveryPackage)
                .map(ConfirmableDeliveryPackage::getDeliveryPackage)
                .map(deliveryPackage -> ConfirmedDeliveryPackage.builder()
                        .deliveryPackage(deliveryPackage)
                        .build())
                .map(Json::encode)
                .subscribe(
                        json -> vertx.eventBus().publish(VertxEvents.deliveryPackageServiceProviderConfirmed, json),
                        t -> {
                            log.error("error while confirming deliveries {}: {}", deliveryIds, t.getMessage());
                            if (log.isDebugEnabled()) {
                                log.debug("", t);
                            }
                        },
                        () -> log.info("Successfully confirmed deliveries {}", deliveryIds));
    }


}
