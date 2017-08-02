package org.amv.trafficsoft.datahub.xfcd;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Collections;
import java.util.function.Consumer;

@Slf4j
@Builder
public class XfcdGetDataPublisherImpl implements XfcdGetDataPublisher {

    private final XfcdClient xfcdClient;
    private final long contractId;

    @Override
    public void subscribe(Subscriber<? super TrafficsoftDeliveryPackage> subscriber) {
        Consumer<FluxSink<TrafficsoftDeliveryPackage>> fluxSinkConsumer = fluxSink -> xfcdClient
                .getDataAndConfirmDeliveries(contractId, Collections.emptyList())
                .toObservable()
                .map(val -> TrafficsoftDeliveryPackageImpl.builder()
                        .deliveries(val)
                        .contractId(contractId)
                        .build())
                .subscribe(fluxSink::next,
                        fluxSink::error,
                        fluxSink::complete);

        Flux.create(fluxSinkConsumer)
                .subscribe(subscriber);
    }
}
