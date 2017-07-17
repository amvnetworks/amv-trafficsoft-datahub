package org.amv.trafficsoft.datahub.xfcd;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Consumer;

@Slf4j
public class TrafficsoftDeliveryPackageHotFlux {
    private final Flux<TrafficsoftDeliveryPackage> deliveryPackageFlux;

    @Builder
    TrafficsoftDeliveryPackageHotFlux(XfcdClient xfcdClient, long contractId) {
        Consumer<FluxSink<TrafficsoftDeliveryPackage>> fluxSinkConsumer = fluxSink -> xfcdClient
                .getDataAndConfirmDeliveries(contractId, Collections.emptyList())
                .toObservable()
                .doOnNext(list -> log.info("Fetched {} deliveries", list.size()))
                .map(val -> TrafficsoftDeliveryPackageImpl.builder()
                        .deliveries(val)
                        .build())
                .subscribe(fluxSink::next,
                        fluxSink::error,
                        fluxSink::complete);

        // TODO: uncomment following code the fetch from actual api endpoint
        //this.deliveryPackageFlux = createHotFlux(fluxSinkConsumer);
        this.deliveryPackageFlux = Flux.interval(Duration.ofSeconds(10))
                .doOnNext(i -> log.info("Starting {} run of hotflux", i))
                .map(i -> (TrafficsoftDeliveryPackage) TrafficsoftDeliveryPackageImpl.builder()
                        .deliveries(DeliveryRestDtoMother.randomList())
                        .build())
                .retry()
                .publish()
                .autoConnect();
    }

    private Flux<TrafficsoftDeliveryPackage> createHotFlux(Consumer<FluxSink<TrafficsoftDeliveryPackage>> fluxSinkConsumer) {
        return Flux.interval(Duration.ofSeconds(10))
                .doOnNext(i -> log.info("Starting {} run of hotflux", i))
                .flatMap(foo -> Flux.create(fluxSinkConsumer))
                .doOnError(i -> log.error("", i))
                .retry()
                //.onErrorResume(t -> true, t -> createHotFlux(fluxSinkConsumer))
                .publish()
                .autoConnect();
    }

    public Flux<TrafficsoftDeliveryPackage> flux() {
        return this.deliveryPackageFlux;
    }
}
