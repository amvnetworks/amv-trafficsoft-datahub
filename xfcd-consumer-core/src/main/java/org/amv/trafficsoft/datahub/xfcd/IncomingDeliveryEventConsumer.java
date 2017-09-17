package org.amv.trafficsoft.datahub.xfcd;

import org.amv.trafficsoft.datahub.xfcd.event.DeliveryEventConsumer;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.TrafficsoftEvent;

public interface IncomingDeliveryEventConsumer extends DeliveryEventConsumer {

    default <E extends TrafficsoftEvent> boolean supports(Class<E> clazz) {
        return IncomingDeliveryEvent.class.isAssignableFrom(clazz);
    }

    default void accept(Object var1) {
        this.accept((IncomingDeliveryEvent) var1, Confirmation.doNothing());
    }

    void accept(IncomingDeliveryEvent var1, Confirmation confirmation);

}
