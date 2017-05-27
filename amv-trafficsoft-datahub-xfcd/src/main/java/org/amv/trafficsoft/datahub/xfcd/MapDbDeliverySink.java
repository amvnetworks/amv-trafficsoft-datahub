package org.amv.trafficsoft.datahub.xfcd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import lombok.Builder;
import lombok.Value;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.mapdb.HTreeMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Optional;

public class MapDbDeliverySink {
    @Value
    @Builder
    public static class DeliveryDatabase {
        private static final ObjectMapper objectMapper = new ObjectMapper();

        private final HTreeMap<Long, String> deliveriesMap;

        public Flux<DeliveryRestDto> save(DeliveryRestDto deliveryRestDto) {
            return Flux.create(fluxSink -> {
                try {
                    String json = objectMapper.writeValueAsString(deliveryRestDto);
                    deliveriesMap.put(deliveryRestDto.getDeliveryId(), json);

                    fluxSink.next(deliveryRestDto);
                    fluxSink.complete();
                } catch (JsonProcessingException e) {
                    fluxSink.error(e);
                }
            });
        }

        public Mono<DeliveryRestDto> getById(long deliveryId) {
            return Mono.fromCallable(() -> Optional.ofNullable(deliveriesMap.get(deliveryId))
                    .map(json -> {
                        try {
                            return objectMapper.readValue(json, DeliveryRestDto.class);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }))
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        }
    }

    private final DeliveryDatabase deliveryDatabase;
    private final AsyncEventBus asyncEventBus;

    public MapDbDeliverySink(AsyncEventBus asyncEventBus, DeliveryDatabase deliveryDatabase) {
        this.asyncEventBus = asyncEventBus;
        this.deliveryDatabase = deliveryDatabase;

        asyncEventBus.register(this);
    }

    @Subscribe
    public void onNext(DeliveryRestDto value) {
        deliveryDatabase.save(value);

        Flux.fromIterable(ImmutableList.of(value))
                .publishOn(Schedulers.elastic())
                .map(delivery -> HandledDelivery.builder()
                        .delivery(delivery)
                        .build())
                .subscribe(asyncEventBus::post);
    }

    @Value
    @Builder
    public static class HandledDelivery {
        private DeliveryRestDto delivery;
    }
}
