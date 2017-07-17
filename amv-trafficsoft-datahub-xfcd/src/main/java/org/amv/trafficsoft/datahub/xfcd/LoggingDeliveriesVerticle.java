package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.event.VertxEvents;

import static java.util.Optional.ofNullable;

@Slf4j
public class LoggingDeliveriesVerticle extends AbstractVerticle {
    private volatile MessageConsumer<String> deliveryPackageConsumer;
    private volatile MessageConsumer<String> deliveryPackageInternallyConfirmedConsumer;
    private volatile MessageConsumer<String> deliveryPackageSavedConsumer;
    private volatile MessageConsumer<String> deliveryPackageServiceProviderConfirmedConsumer;

    @Builder
    LoggingDeliveriesVerticle() {
    }

    @Override
    public void start() throws Exception {
        this.deliveryPackageConsumer = vertx.eventBus().consumer(VertxEvents.deliveryPackage, new Handler<Message<String>>() {
            public void handle(Message<String> objectMessage) {
                TrafficsoftDeliveryPackageImpl deliveryPackage = Json.decodeValue(objectMessage.body(), TrafficsoftDeliveryPackageImpl.class);
                log.info("received event '{}': {}", VertxEvents.deliveryPackage, deliveryPackage.getDelivieryIds());
            }
        });

        this.deliveryPackageInternallyConfirmedConsumer = vertx.eventBus().consumer(VertxEvents.deliveryPackageInternallyConfirmed, new Handler<Message<String>>() {
            public void handle(Message<String> objectMessage) {
                ConfirmableDeliveryPackage confirmableDeliveryPackage = Json.decodeValue(objectMessage.body(), ConfirmableDeliveryPackage.class);
                log.info("received event '{}': {}", VertxEvents.deliveryPackageInternallyConfirmed, confirmableDeliveryPackage
                        .getDeliveryPackage().getDelivieryIds());
            }
        });

        this.deliveryPackageSavedConsumer = vertx.eventBus().consumer(VertxEvents.deliveryPackageSaved, new Handler<Message<String>>() {
            public void handle(Message<String> objectMessage) {
                ConfirmableDeliveryPackage confirmableDeliveryPackage = Json.decodeValue(objectMessage.body(), ConfirmableDeliveryPackage.class);
                log.info("received event '{}': {}", VertxEvents.deliveryPackageSaved, confirmableDeliveryPackage
                        .getDeliveryPackage().getDelivieryIds());
            }
        });

        this.deliveryPackageServiceProviderConfirmedConsumer = vertx.eventBus().consumer(VertxEvents.deliveryPackageServiceProviderConfirmed, new Handler<Message<String>>() {
            public void handle(Message<String> objectMessage) {
                ConfirmableDeliveryPackage confirmableDeliveryPackage = Json.decodeValue(objectMessage.body(), ConfirmableDeliveryPackage.class);
                log.info("received event '{}': {}", VertxEvents.deliveryPackageServiceProviderConfirmed, confirmableDeliveryPackage
                        .getDeliveryPackage().getDelivieryIds());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        ofNullable(deliveryPackageConsumer).ifPresent(MessageConsumer::unregister);
        ofNullable(deliveryPackageInternallyConfirmedConsumer).ifPresent(MessageConsumer::unregister);
        ofNullable(deliveryPackageSavedConsumer).ifPresent(MessageConsumer::unregister);
        ofNullable(deliveryPackageServiceProviderConfirmedConsumer).ifPresent(MessageConsumer::unregister);
    }

}
