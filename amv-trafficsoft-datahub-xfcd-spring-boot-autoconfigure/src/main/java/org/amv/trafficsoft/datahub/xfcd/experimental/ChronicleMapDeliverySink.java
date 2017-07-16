package org.amv.trafficsoft.datahub.xfcd.experimental;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.openhft.chronicle.map.ChronicleMap;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.event.HandledDeliveryPackage;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChronicleMapDeliverySink {

    private final ChronicleMap<Long, DeliveryRestDto> deliveryDatabase;
    private final EventBus eventBus;

    public ChronicleMapDeliverySink(EventBus eventBus, ChronicleMap<Long, DeliveryRestDto> deliveryDatabase) {
        this.eventBus = eventBus;
        this.deliveryDatabase = deliveryDatabase;

        eventBus.register(this);
    }

    @Subscribe
    public void onNext(TrafficsoftDeliveryPackage value) {
        final Map<Long, DeliveryRestDto> deliveriesById = value.getDeliveries().stream()
                .collect(Collectors.toMap(DeliveryRestDto::getDeliveryId, Function.identity()));

        deliveryDatabase.putAll(deliveriesById);

        Flux.fromIterable(ImmutableList.of(value))
                .publishOn(Schedulers.elastic())
                .subscribeOn(Schedulers.elastic())
                .map(delivery -> HandledDeliveryPackage.builder()
                        .delivery(delivery)
                        .build())
                .subscribe(eventBus::post);
    }

}
