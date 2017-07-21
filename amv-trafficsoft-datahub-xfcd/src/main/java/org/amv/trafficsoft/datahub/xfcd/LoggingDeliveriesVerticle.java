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

@Slf4j
public class LoggingDeliveriesVerticle extends AbstractVerticle {
    private final XfcdEvents xfcdEvents;

    private final List<BaseSubscriber<?>> subscribers = Lists.newArrayList();

    @Builder
    LoggingDeliveriesVerticle(XfcdEvents xfcdEvents) {
        this.xfcdEvents = xfcdEvents;
    }

    @Override
    public void start() throws Exception {
        final BaseSubscriber<IncomingDeliveryEvent> incomingDeliveryEventBaseSubscriber = new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent value) {
                log.info("received event '{}': {}", value.getClass(), value.getDeliveryPackage().getDelivieryIds());
            }
        };
        subscribers.add(incomingDeliveryEventBaseSubscriber);
        xfcdEvents.subscribe(IncomingDeliveryEvent.class, incomingDeliveryEventBaseSubscriber);

        final BaseSubscriber<ConfirmableDeliveryEvent> confirmableDeliveryEventBaseSubscriber = new BaseSubscriber<ConfirmableDeliveryEvent>() {
            @Override
            protected void hookOnNext(ConfirmableDeliveryEvent value) {
                log.info("received event '{}': {}", value.getClass(), value.getDeliveryPackage().getDelivieryIds());

            }
        };
        subscribers.add(confirmableDeliveryEventBaseSubscriber);
        xfcdEvents.subscribe(ConfirmableDeliveryEvent.class, confirmableDeliveryEventBaseSubscriber);

        final BaseSubscriber<ConfirmedDeliveryEvent> confirmedDeliveryEventBaseSubscriber = new BaseSubscriber<ConfirmedDeliveryEvent>() {
            @Override
            protected void hookOnNext(ConfirmedDeliveryEvent value) {
                log.info("received event '{}': {}", value.getClass(), value.getDeliveryPackage().getDelivieryIds());
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
