package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.collect.ImmutableList;
import io.vertx.rxjava3.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmedDeliveryEvent;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * A verticle that listens for {@link ConfirmableDeliveryEvent} representing
 * a successfully processed {@link TrafficsoftDeliveryPackage} and
 * and notifies AMV TrafficSoft xfcd API about it.
 */
@Slf4j
public class DeliveryConfirmationVerticle extends AbstractVerticle {
    private final XfcdEvents xfcdEvents;
    private final XfcdClient xfcdClient;
    private final long contractId;

    private BaseSubscriber<ConfirmableDeliveryEvent> subscriber;

    public DeliveryConfirmationVerticle(XfcdEvents xfcdEvents,
                                        XfcdClient xfcdClient,
                                        long contractId) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
        this.xfcdClient = requireNonNull(xfcdClient);
        this.contractId = contractId;
    }

    @Override
    public void start() throws Exception {
        this.subscriber = new BaseSubscriber<ConfirmableDeliveryEvent>() {
            @Override
            protected void hookOnNext(ConfirmableDeliveryEvent event) {
                try {
                    final TrafficsoftDeliveryPackage deliveryPackage = event.getDeliveryPackage();
                    final int amountOfNodes = deliveryPackage.getAmountOfNodes();

                    log.info("Received event in DeliveryConfirmationVerticle '{}' with {} nodes: {}", event.getClass().getSimpleName(),
                            amountOfNodes,
                            deliveryPackage.getDeliveryIds());

                    onConfirmableDeliveryEvent(event);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        };
        log.info("Subscribing to {} on Vert.x event bus", ConfirmableDeliveryEvent.class.getSimpleName());
        xfcdEvents.subscribe(ConfirmableDeliveryEvent.class, this.subscriber);
    }

    @Override
    public void stop() throws Exception {
        ofNullable(this.subscriber).ifPresent(BaseSubscriber::dispose);
    }

    private void onConfirmableDeliveryEvent(ConfirmableDeliveryEvent confirmableDeliveryEvent) {
        confirmDeliveries(confirmableDeliveryEvent);
    }

    private void confirmDeliveries(ConfirmableDeliveryEvent confirmableDeliveryEvent) {
        Set<Long> deliveryIds = Stream.of(confirmableDeliveryEvent)
                .map(ConfirmableDeliveryEvent::getDeliveryPackage)
                .map(TrafficsoftDeliveryPackage::getDeliveries)
                .flatMap(Collection::stream)
                .map(DeliveryRestDto::getDeliveryId)
                .collect(Collectors.toSet());

        if (log.isDebugEnabled()) {
            log.debug("Preparing to confirm {} deliveries for contract {}: {}", deliveryIds.size(), contractId, deliveryIds);
        }

        xfcdClient.confirmDeliveries(contractId, ImmutableList.copyOf(deliveryIds))
                .toObservable()
                .retry(3)
                .map(foo -> confirmableDeliveryEvent)
                .map(ConfirmableDeliveryEvent::getDeliveryPackage)
                .map(deliveryPackage -> ConfirmedDeliveryEvent.builder()
                        .deliveryPackage(deliveryPackage)
                        .build())
                .subscribe(val -> {
                            xfcdEvents.publish(ConfirmedDeliveryEvent.class, Flux.just(val));
                        },
                        t -> {
                            String message = t.getMessage();
                            String causeMessage = Optional.ofNullable(t.getCause())
                                    .map(Throwable::getMessage)
                                    .orElse(null);

                            log.error("Error while confirming deliveries: {}\n" +
                                    "Origin: {}\n" +
                                    "Cause: {}\n", deliveryIds, message, causeMessage);

                            if (log.isDebugEnabled()) {
                                log.debug("", t);
                            }
                        },
                        () -> {
                            if (log.isDebugEnabled()) {
                                log.debug("Successfully confirmed deliveries {}", deliveryIds);
                            }
                        });
    }


}
