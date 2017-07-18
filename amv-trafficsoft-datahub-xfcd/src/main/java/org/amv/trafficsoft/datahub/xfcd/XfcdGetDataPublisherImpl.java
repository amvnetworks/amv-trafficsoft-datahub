package org.amv.trafficsoft.datahub.xfcd;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.amv.trafficsoft.rest.xfcd.model.TrackRestDtoMother;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.sql.Date;
import java.time.Instant;
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
                .doOnNext(list -> log.info("Fetched {} deliveries", list.size()))
                .map(val -> TrafficsoftDeliveryPackageImpl.builder()
                        .deliveries(val)
                        .contractId(contractId)
                        .build())
                // TODO on error Return Random -> REMOVE AFTER DEBUGGING
                .onErrorReturn(t -> TrafficsoftDeliveryPackageImpl.builder()
                        .deliveries(DeliveryRestDtoMother.randomList())
                        .addDelivery(DeliveryRestDto.builder()
                                .deliveryId(1L)
                                .timestamp(Date.from(Instant.now()))
                                .track(TrackRestDtoMother.randomList())
                                .build())
                        .contractId(contractId)
                        .build())
                .subscribe(fluxSink::next,
                        fluxSink::error,
                        fluxSink::complete);

        Flux.create(fluxSinkConsumer)
                .subscribe(subscriber);
    }
}
