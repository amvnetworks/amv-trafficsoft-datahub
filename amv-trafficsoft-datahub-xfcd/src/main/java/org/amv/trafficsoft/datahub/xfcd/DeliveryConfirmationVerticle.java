package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.collect.ImmutableList;
import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmedDeliveryEvent;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.Collection;
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

    @Builder
    DeliveryConfirmationVerticle(XfcdEvents xfcdEvents,
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
                confirm(event);
            }
        };
        xfcdEvents.subscribe(ConfirmableDeliveryEvent.class, this.subscriber);
    }

    @Override
    public void stop() throws Exception {
        ofNullable(this.subscriber).ifPresent(BaseSubscriber::dispose);
    }

    private void confirm(ConfirmableDeliveryEvent confirmableDeliveryEvent) {
        Set<Long> deliveryIds = Stream.of(confirmableDeliveryEvent)
                .map(ConfirmableDeliveryEvent::getDeliveryPackage)
                .map(TrafficsoftDeliveryPackage::getDeliveries)
                .flatMap(Collection::stream)
                .map(DeliveryRestDto::getDeliveryId)
                .collect(Collectors.toSet());

        xfcdClient.confirmDeliveries(contractId, ImmutableList.copyOf(deliveryIds))
                .toObservable()
                .map(foo -> confirmableDeliveryEvent)
                .map(ConfirmableDeliveryEvent::getDeliveryPackage)
                .map(deliveryPackage -> ConfirmedDeliveryEvent.builder()
                        .deliveryPackage(deliveryPackage)
                        .build())
                .subscribe(val -> xfcdEvents.publish(ConfirmedDeliveryEvent.class, Flux.just(val)), t -> {
                            log.error("Error while confirming deliveries {}: {}", deliveryIds, t.getMessage());
                            if (log.isDebugEnabled()) {
                                log.debug("", t);
                            }
                        },
                        () -> log.info("Successfully confirmed deliveries {}", deliveryIds));
    }


}
