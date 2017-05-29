package org.amv.trafficsoft.datahub.xfcd.experimental;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.openhft.chronicle.map.ChronicleMap;
import org.amv.trafficsoft.datahub.xfcd.event.HandledDelivery;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ChronicleMapDeliverySink {

    private final ChronicleMap<Long, DeliveryRestDto> deliveryDatabase;
    private final EventBus eventBus;

    public ChronicleMapDeliverySink(EventBus eventBus, ChronicleMap<Long, DeliveryRestDto> deliveryDatabase) {
        this.eventBus = eventBus;
        this.deliveryDatabase = deliveryDatabase;

        eventBus.register(this);
    }

    @Subscribe
    public void onNext(DeliveryRestDto value) {
        deliveryDatabase.put(value.getDeliveryId(), value);

        Flux.fromIterable(ImmutableList.of(value))
                .publishOn(Schedulers.elastic())
                .subscribeOn(Schedulers.elastic())
                .map(delivery -> HandledDelivery.builder()
                        .delivery(delivery)
                        .build())
                .subscribe(eventBus::post);
    }

}
