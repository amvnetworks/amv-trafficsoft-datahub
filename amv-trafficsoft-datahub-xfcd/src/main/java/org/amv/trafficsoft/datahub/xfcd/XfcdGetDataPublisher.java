package org.amv.trafficsoft.datahub.xfcd;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Collections;
import java.util.function.Consumer;

@Slf4j
@Builder
public class XfcdGetDataPublisher implements Publisher<DeliveryRestDto> {

    private final XfcdClient xfcdClient;
    private final long contractId;

    @Override
    public void subscribe(Subscriber<? super DeliveryRestDto> subscriber) {
        Consumer<FluxSink<DeliveryRestDto>> fluxSinkConsumer = fluxSink -> xfcdClient
                .getDataAndConfirmDeliveries(contractId, Collections.emptyList())
                .toObservable()
                .doOnNext(list -> log.info("Fetched {} delivieries", list.size()))
                .flatMapIterable(list -> list)
                .subscribe(fluxSink::next,
                        fluxSink::error,
                        fluxSink::complete);

        Flux.create(fluxSinkConsumer)
                .subscribe(subscriber);
    }
}
