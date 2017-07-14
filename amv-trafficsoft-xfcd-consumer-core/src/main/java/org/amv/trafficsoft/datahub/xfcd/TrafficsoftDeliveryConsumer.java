package org.amv.trafficsoft.datahub.xfcd;


import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.reactivestreams.Subscriber;

import java.util.List;

public interface TrafficsoftDeliveryConsumer extends Subscriber<TrafficsoftDeliveryPackage> {

}
