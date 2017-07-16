package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;

import java.util.function.Supplier;

@Slf4j
public class TrafficsoftDeliveryPackageSubscriberEventBusAdapter extends SubscriberEventBusAdapter<TrafficsoftDeliveryPackage> {

    public TrafficsoftDeliveryPackageSubscriberEventBusAdapter(Supplier<Subscriber<TrafficsoftDeliveryPackage>> subscriberSupplier,
                                                               EventBus eventBus) {
        super(subscriberSupplier, eventBus);
    }
}
