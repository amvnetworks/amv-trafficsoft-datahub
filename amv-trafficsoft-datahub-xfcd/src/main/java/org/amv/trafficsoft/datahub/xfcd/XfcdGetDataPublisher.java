package org.amv.trafficsoft.datahub.xfcd;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.WorkQueueProcessor;

import java.util.Collections;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Slf4j
public class XfcdGetDataPublisher implements Publisher<DeliveryRestDto> {

    private final XfcdClient xfcdClient;
    private final long contractId;

    Flux<DeliveryRestDto> deliveryRestDtoFlux;

    public XfcdGetDataPublisher(XfcdClient xfcdClient, long contractId) {
        this.xfcdClient = requireNonNull(xfcdClient);
        this.contractId = contractId;

        Consumer<FluxSink<DeliveryRestDto>> fluxSinkConsumer = fluxSink -> {
            xfcdClient
                    .getDataAndConfirmDeliveries(contractId, Collections.emptyList())
                    .toObservable()
                    .flatMapIterable(i -> i)
                    .subscribe(fluxSink::next,
                            fluxSink::error,
                            fluxSink::complete);
        };

        this.deliveryRestDtoFlux = Flux.create(fluxSinkConsumer);
    }

    @Override
    public void subscribe(Subscriber<? super DeliveryRestDto> subscriber) {



        deliveryRestDtoFlux.subscribe(subscriber);
    }
}
