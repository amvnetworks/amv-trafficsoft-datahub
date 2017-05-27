package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.MapDbDeliverySink.HandledDelivery;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

import java.util.Collections;

import static java.util.Objects.requireNonNull;

@Slf4j
@Builder
public class HandledDeliveryHandler {

    private final AsyncEventBus asyncEventBus;
    private final XfcdClient xfcdClient;
    private final long contractId;

    public HandledDeliveryHandler(AsyncEventBus asyncEventBus, XfcdClient xfcdClient, long contractId) {
        this.asyncEventBus = asyncEventBus;
        this.xfcdClient = requireNonNull(xfcdClient);
        this.contractId = contractId;

        asyncEventBus.register(this);
    }

    @Subscribe
    public void onNext(HandledDelivery value) {
        long deliveryId = value.getDelivery().getDeliveryId();
        log.info("About to confirm delivery {}", deliveryId);

        xfcdClient.confirmDeliveries(contractId, Collections.singletonList(deliveryId))
                .toObservable()
                .map(foo -> ConfirmedDelivery.builder()
                        .delivery(value.getDelivery())
                        .build())
                .doOnNext(foo -> log.info("Confirmed delivery {}", deliveryId))
                .subscribe(asyncEventBus::post);
    }

    @Value
    @Builder
    public static class ConfirmedDelivery {
        private DeliveryRestDto delivery;
    }
}
