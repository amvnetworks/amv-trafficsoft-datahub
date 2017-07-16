package org.amv.trafficsoft.datahub.xfcd.experimental;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import lombok.Builder;
import lombok.Value;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.event.HandledDeliveryPackage;
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

        public Mono<TrafficsoftDeliveryPackage> saveAll(TrafficsoftDeliveryPackage deliveries) {
            return Mono.create(fluxSink -> {
                try {
                    for (DeliveryRestDto deliveryRestDto : deliveries.getDeliveries()) {
                        String json = objectMapper.writeValueAsString(deliveryRestDto);
                        deliveriesMap.put(deliveryRestDto.getDeliveryId(), json);
                    }

                    fluxSink.success(deliveries);
                } catch (JsonProcessingException e) {
                    fluxSink.error(e);
                }
            });
        }

        public Mono<DeliveryRestDto> save(DeliveryRestDto deliveryRestDto) {
            return Mono.create(fluxSink -> {
                try {
                    String json = objectMapper.writeValueAsString(deliveryRestDto);
                    deliveriesMap.put(deliveryRestDto.getDeliveryId(), json);

                    fluxSink.success(deliveryRestDto);
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
    public void onNext(TrafficsoftDeliveryPackage value) {
        deliveryDatabase.saveAll(value)
                .publishOn(Schedulers.elastic())
                .subscribeOn(Schedulers.elastic())
                .map(delivery -> HandledDeliveryPackage.builder()
                        .delivery(value)
                        .build())
                .subscribe(asyncEventBus::post);
    }

}
