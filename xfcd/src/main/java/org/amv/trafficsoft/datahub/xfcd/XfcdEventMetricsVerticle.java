package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.collect.Lists;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import io.vertx.rxjava.core.AbstractVerticle;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmedDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import reactor.core.publisher.BaseSubscriber;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class XfcdEventMetricsVerticle extends AbstractVerticle {
    private static final Summary incomingDeliveryNodeCountSummary = Summary.build()
            .name("datahub_incoming_delivery_nodes")
            .help("Summary of amount of incoming nodes")
            .quantile(0.5, 0.05)   // Add 50th percentile (= median) with 5% tolerated error
            .quantile(0.9, 0.01)   // Add 90th percentile with 1% tolerated error
            .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
            .register();

    private static final Counter incomingDeliveryCounter = Counter.build()
            .name("datahub_incoming_delivery_count")
            .help("Counter of incoming deliveries")
            .register();

    private static final Counter confirmedDeliveryCounter = Counter.build()
            .name("datahub_confirmed_delivery_count")
            .help("Counter of confirmed deliveries")
            .register();

    private final XfcdEvents xfcdEvents;

    private final List<BaseSubscriber<?>> subscribers = Lists.newArrayList();

    public XfcdEventMetricsVerticle(XfcdEvents xfcdEvents) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
    }

    @Override
    public void start() throws Exception {
        final BaseSubscriber<IncomingDeliveryEvent> incomingDeliveryEventBaseSubscriber = new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent value) {
                incomingDeliveryCounter.inc();

                TrafficsoftDeliveryPackage deliveryPackage = value.getDeliveryPackage();
                int amountOfNodes = deliveryPackage.getAmountOfNodes();

                incomingDeliveryNodeCountSummary.observe(amountOfNodes);
            }
        };
        subscribers.add(incomingDeliveryEventBaseSubscriber);
        xfcdEvents.subscribe(IncomingDeliveryEvent.class, incomingDeliveryEventBaseSubscriber);

        final BaseSubscriber<ConfirmedDeliveryEvent> confirmedDeliveryEventBaseSubscriber = new BaseSubscriber<ConfirmedDeliveryEvent>() {
            @Override
            protected void hookOnNext(ConfirmedDeliveryEvent value) {
                confirmedDeliveryCounter.inc();
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
