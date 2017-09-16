package org.amv.trafficsoft.datahub.xfcd.event;

import java.util.function.Consumer;

public interface DeliveryEventConsumer extends Consumer<Object> {
    <E extends TrafficsoftEvent> boolean supports(Class<E> clazz);
}
