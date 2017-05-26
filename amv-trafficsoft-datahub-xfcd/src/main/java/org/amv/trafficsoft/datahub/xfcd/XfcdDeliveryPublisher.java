package org.amv.trafficsoft.datahub.xfcd;

import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import rx.Observable;

public interface XfcdDeliveryPublisher extends Publisher<DeliveryRestDto> {

}
