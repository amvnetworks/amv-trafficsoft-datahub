package org.amv.trafficsoft.datahub.xfcd;

import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

public interface XfcdDeliveryRestDtoSubscriber extends Subscriber<DeliveryRestDto> {
}
