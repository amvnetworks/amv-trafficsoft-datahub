package org.amv.trafficsoft.datahub.xfcd;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Builder
public class TrafficsoftDeliveryPublisherImpl implements TrafficsoftDeliveryPublisher {

    private final XfcdClient xfcdClient;
    private final long contractId;

    @Override
    public void subscribe(Subscriber<? super TrafficsoftDeliveryPackage> subscriber) {
        Consumer<FluxSink<TrafficsoftDeliveryPackage>> fluxSinkConsumer = fluxSink -> {
            try {
                List<DeliveryRestDto> deliveryRestDtos = xfcdClient
                        .getDataAndConfirmDeliveries(contractId, Collections.emptyList())
                        .execute();

                TrafficsoftDeliveryPackage deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                        .deliveries(deliveryRestDtos)
                        .contractId(contractId)
                        .build();

                fluxSink.next(deliveryPackage);
                fluxSink.complete();
            } catch (Exception e) {
                fluxSink.error(e);
            }
        };

        Flux.create(fluxSinkConsumer)
                .subscribe(subscriber);
    }
}
