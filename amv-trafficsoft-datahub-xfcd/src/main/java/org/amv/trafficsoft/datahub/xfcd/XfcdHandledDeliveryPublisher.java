package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.EventBus;
import org.amv.trafficsoft.datahub.xfcd.MapDbDeliverySink.HandledDelivery;

public class XfcdHandledDeliveryPublisher extends GuavaEventBusReactorPublisher<HandledDelivery> {

    public XfcdHandledDeliveryPublisher(EventBus eventBus) {
        super(eventBus);
    }
}
