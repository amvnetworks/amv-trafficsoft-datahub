package org.amv.trafficsoft.datahub.xfcd;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Collections;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Slf4j
public class GetDataPublisher implements Publisher<DeliveryRestDto> {

    private final XfcdClient xfcdClient;
    private final long contractId;

    public GetDataPublisher(XfcdClient xfcdClient, long contractId) {
        this.xfcdClient = requireNonNull(xfcdClient);
        this.contractId = contractId;
    }

    @Override
    public void subscribe(Subscriber<? super DeliveryRestDto> subscriber) {
        Flux.create((Consumer<FluxSink<DeliveryRestDto>>) fluxSink -> xfcdClient
                .getDataAndConfirmDeliveries(contractId, Collections.emptyList())
                .toObservable()
                .flatMapIterable(i -> i)
                .subscribe(fluxSink::next,
                        fluxSink::error,
                        fluxSink::complete))
                .subscribe(subscriber);
    }
}
