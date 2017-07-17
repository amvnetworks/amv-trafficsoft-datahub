package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmedDeliveryPackage;

@Slf4j
public class ConfirmDeliveriesVerticle extends AbstractVerticle {
    MessageConsumer<String> consumer;

    @Builder
    ConfirmDeliveriesVerticle() {
    }

    @Override
    public void start() throws Exception {
        this.consumer = vertx.eventBus().consumer(VertxMessages.deliveryPackage, new Handler<Message<String>>() {
            public void handle(Message<String> objectMessage) {
                final TrafficsoftDeliveryPackageImpl trafficsoftDeliveryPackage = Json.decodeValue(objectMessage.body(), TrafficsoftDeliveryPackageImpl.class);

                vertx.eventBus().publish(VertxMessages.deliveryPackageConsumed, Json.encode(ConfirmedDeliveryPackage.builder()
                        .delivery(trafficsoftDeliveryPackage)
                        .build()));
            }
        });
    }

    @Override
    public void stop() throws Exception {
        consumer.unregister();
    }

}
