package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.collect.Lists;
import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmedDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import reactor.core.publisher.BaseSubscriber;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class XfcdEventLoggingVerticle extends AbstractVerticle {
    private final XfcdEvents xfcdEvents;

    private final List<BaseSubscriber<?>> subscribers = Lists.newArrayList();

    @Builder
    XfcdEventLoggingVerticle(XfcdEvents xfcdEvents) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
    }

    @Override
    public void start() throws Exception {
        final BaseSubscriber<IncomingDeliveryEvent> incomingDeliveryEventBaseSubscriber = new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent value) {
                final TrafficsoftDeliveryPackage deliveryPackage = value.getDeliveryPackage();
                final int amountOfNodes = deliveryPackage.getAmountOfNodes();

                log.info("Received event '{}' with {} nodes: {}", value.getClass().getSimpleName(),
                        amountOfNodes,
                        deliveryPackage.getDeliveryIds());
            }
        };
        subscribers.add(incomingDeliveryEventBaseSubscriber);
        xfcdEvents.subscribe(IncomingDeliveryEvent.class, incomingDeliveryEventBaseSubscriber);

        final BaseSubscriber<ConfirmableDeliveryEvent> confirmableDeliveryEventBaseSubscriber = new BaseSubscriber<ConfirmableDeliveryEvent>() {
            @Override
            protected void hookOnNext(ConfirmableDeliveryEvent value) {
                log.info("Received event '{}': {}", value.getClass().getSimpleName(), value.getDeliveryPackage().getDeliveryIds());
            }
        };
        subscribers.add(confirmableDeliveryEventBaseSubscriber);
        xfcdEvents.subscribe(ConfirmableDeliveryEvent.class, confirmableDeliveryEventBaseSubscriber);

        final BaseSubscriber<ConfirmedDeliveryEvent> confirmedDeliveryEventBaseSubscriber = new BaseSubscriber<ConfirmedDeliveryEvent>() {
            @Override
            protected void hookOnNext(ConfirmedDeliveryEvent value) {
                log.info("Received event '{}': {}", value.getClass().getSimpleName(), value.getDeliveryPackage().getDeliveryIds());
            }
        };
        subscribers.add(confirmedDeliveryEventBaseSubscriber);
        xfcdEvents.subscribe(ConfirmedDeliveryEvent.class, confirmedDeliveryEventBaseSubscriber);
    }


    @Override
    public void stop() throws Exception {
        subscribers.forEach(BaseSubscriber::dispose);
    }
}
